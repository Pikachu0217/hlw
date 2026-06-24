package com.hlw.patient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.patient.dto.CreateHealthRecordRequest;
import com.hlw.patient.dto.CreatePatientRequest;
import com.hlw.patient.dto.UpdatePatientProfileRequest;
import com.hlw.patient.domain.resp.InternalPatientResp;
import com.hlw.patient.entity.PatHealthRecordEntity;
import com.hlw.patient.entity.PatPatientEntity;
import com.hlw.patient.mapper.PatHealthRecordMapper;
import com.hlw.patient.mapper.PatPatientMapper;
import com.hlw.patient.vo.HealthRecordVO;
import com.hlw.patient.vo.PatientProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 患者模块租户上下文服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientTenantContextService {
    private static final String DEFAULT_RISK_LEVEL = "低风险";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 患者档案数据访问组件。 */
    private final PatPatientMapper patPatientMapper;
    /** 健康档案数据访问组件。 */
    private final PatHealthRecordMapper patHealthRecordMapper;

    /**
     * 查询当前患者档案。
     *
     * @return 患者档案展示对象
     */
    public PatientProfileVO getCurrentProfile() {
        log.info("查询当前患者档案");
        return toPatientProfileVO(getOrCreateCurrentPatient());
    }

    /**
     * 更新当前患者档案（患者不存在时自动创建）。
     *
     * @param request 更新患者资料请求
     * @return 患者档案展示对象
     */
    @Transactional
    public PatientProfileVO updateCurrentProfile(UpdatePatientProfileRequest request) {
        ensureBusinessTenantContext("患者模块操作缺少有效租户上下文");
        PatPatientEntity entity = getOrCreateCurrentPatient();
        log.info("更新当前患者档案，patientId={}，patientName={}", entity.getId(), request.getPatientName());
        applyPatientProfile(entity, request);
        patPatientMapper.updateById(entity);
        return toPatientProfileVO(entity);
    }

    /**
     * 查询患者列表。
     *
     * @return 患者展示列表
     */
    public List<PatientProfileVO> listPatients() {
        log.info("查询患者列表");
        List<PatHealthRecordEntity> healthRecords = patHealthRecordMapper.selectList(activeHealthRecordWrapper())
            .stream()
            .sorted(Comparator.comparing(PatHealthRecordEntity::getId).reversed())
            .toList();
        Map<Long, Long> healthRecordCountMap = healthRecords.stream()
            .collect(Collectors.groupingBy(PatHealthRecordEntity::getPatientId, Collectors.counting()));
        Map<Long, PatHealthRecordEntity> latestRecordMap = healthRecords.stream()
            .collect(Collectors.toMap(
                PatHealthRecordEntity::getPatientId,
                Function.identity(),
                (current, ignored) -> current
            ));
        return patPatientMapper.selectList(activePatientWrapper())
            .stream()
            .sorted(Comparator.comparing(PatPatientEntity::getId))
            .map(entity -> toPatientProfileVO(
                entity,
                Math.toIntExact(healthRecordCountMap.getOrDefault(entity.getId(), 0L)),
                latestRecordMap.get(entity.getId())
            ))
            .toList();
    }

    /**
     * 按租户和登录用户查询内部患者档案。
     *
     * @param tenantId 租户编号
     * @param userId 登录用户编号
     * @return 内部患者档案
     */
    public InternalPatientResp getInternalPatientByUser(Long tenantId, String userId) {
        log.info("按登录用户查询内部患者档案，tenantId={}，userId={}", tenantId, userId);
        if (tenantId == null || tenantId <= 0L || !StringUtils.hasText(userId)) {
            log.warn("查询内部患者档案失败，租户或用户编号无效，tenantId={}，userId={}", tenantId, userId);
            throw new BizException(400, "租户或用户编号无效");
        }
        PatPatientEntity entity = patPatientMapper.selectOne(new LambdaQueryWrapper<PatPatientEntity>()
            .eq(PatPatientEntity::getTenantId, tenantId)
            .eq(PatPatientEntity::getUserId, userId)
            .eq(PatPatientEntity::getDeleted, 0)
            .last("limit 1"));
        if (entity == null) {
            log.warn("登录用户未绑定患者档案，tenantId={}，userId={}", tenantId, userId);
            throw new BizException(403, "当前登录账号未绑定患者档案");
        }
        return new InternalPatientResp(entity.getId(), entity.getUserId(), entity.getTenantId(), resolvePatientName(entity));
    }

    /**
     * 创建或获取患者档案（手机号注册后自动绑定）。
     *
     * @param tenantId 租户编号
     * @param userId   关联用户编号（sys_user.user_id 字符串）
     * @param phone    联系电话
     * @return 内部患者档案
     */
    @Transactional
    public InternalPatientResp createOrGetPatientByUser(Long tenantId, String userId, String phone) {
        log.info("创建或获取患者档案，tenantId={}，userId={}，phone={}", tenantId, userId, phone);
        if (tenantId == null || tenantId <= 0L || userId == null || userId.isBlank()) {
            log.warn("创建患者档案失败，租户或用户编号无效，tenantId={}，userId={}", tenantId, userId);
            throw new BizException(400, "租户或用户编号无效");
        }
        // 先查是否存在
        PatPatientEntity existing = patPatientMapper.selectOne(new LambdaQueryWrapper<PatPatientEntity>()
            .eq(PatPatientEntity::getTenantId, tenantId)
            .eq(PatPatientEntity::getUserId, userId)
            .eq(PatPatientEntity::getDeleted, 0)
            .last("limit 1"));
        if (existing != null) {
            log.info("患者档案已存在，tenantId={}，userId={}，patientId={}", tenantId, userId, existing.getId());
            return new InternalPatientResp(existing.getId(), existing.getUserId(), existing.getTenantId(), resolvePatientName(existing));
        }
        // 不存在则创建
        PatPatientEntity entity = new PatPatientEntity();
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setPhone(phone);
        entity.setPatientName("");
        entity.setName("");
        patPatientMapper.insert(entity);
        log.info("患者档案创建成功，tenantId={}，userId={}，patientId={}", tenantId, userId, entity.getId());
        return new InternalPatientResp(entity.getId(), entity.getUserId(), entity.getTenantId(), "");
    }

    /**
     * 查询患者详情。
     *
     * @param id 患者编号
     * @return 患者详情展示对象
     */
    public PatientProfileVO getPatient(Long id) {
        log.info("查询患者详情，patientId={}", id);
        return toPatientProfileVO(requireActivePatient(id));
    }

    /**
     * 创建患者档案。
     *
     * @param request 创建患者请求
     * @return 患者详情展示对象
     */
    @Transactional
    public PatientProfileVO createPatient(CreatePatientRequest request) {
        ensureBusinessTenantContext("患者模块操作缺少有效租户上下文");
        log.info("创建患者档案，patientName={}，phone={}", request.getPatientName(), request.getPhone());
        PatPatientEntity entity = new PatPatientEntity();
        entity.setUserId(request.getUserId());
        applyPatientProfile(entity, request);
        patPatientMapper.insert(entity);
        return toPatientProfileVO(entity);
    }

    /**
     * 更新患者档案。
     *
     * @param id 患者编号
     * @param request 更新患者资料请求
     * @return 患者详情展示对象
     */
    @Transactional
    public PatientProfileVO updatePatient(Long id, UpdatePatientProfileRequest request) {
        ensureBusinessTenantContext("患者模块操作缺少有效租户上下文");
        log.info("更新患者档案，patientId={}，patientName={}", id, request.getPatientName());
        PatPatientEntity entity = requireActivePatient(id);
        applyPatientProfile(entity, request);
        patPatientMapper.updateById(entity);
        return toPatientProfileVO(entity);
    }

    /**
     * 查询健康档案列表。
     *
     * @param patientId 患者编号
     * @return 健康档案展示列表
     */
    public List<HealthRecordVO> listHealthRecords(Long patientId) {
        log.info("查询健康档案列表，patientId={}", patientId);
        LambdaQueryWrapper<PatHealthRecordEntity> wrapper = activeHealthRecordWrapper();
        if (patientId != null) {
            requireActivePatient(patientId);
            wrapper.eq(PatHealthRecordEntity::getPatientId, patientId);
        }
        return patHealthRecordMapper.selectList(wrapper)
            .stream()
            .sorted(Comparator.comparing(PatHealthRecordEntity::getId).reversed())
            .map(this::toHealthRecordVO)
            .toList();
    }

    /**
     * 创建健康档案。
     *
     * @param request 创建健康档案请求
     * @return 健康档案展示对象
     */
    @Transactional
    public HealthRecordVO createHealthRecord(CreateHealthRecordRequest request) {
        ensureBusinessTenantContext("患者模块操作缺少有效租户上下文");
        log.info("创建健康档案，patientId={}，title={}", request.getPatientId(), request.getTitle());
        requireActivePatient(request.getPatientId());
        PatHealthRecordEntity entity = new PatHealthRecordEntity();
        entity.setPatientId(request.getPatientId());
        entity.setTitle(request.getTitle());
        entity.setSummary(request.getSummary());
        entity.setAllergies(defaultIfBlank(request.getAllergies(), ""));
        entity.setHistory(defaultIfBlank(request.getHistory(), ""));
        entity.setDiagnosis(defaultIfBlank(request.getDiagnosis(), ""));
        entity.setRemark(defaultIfBlank(request.getRemark(), ""));
        patHealthRecordMapper.insert(entity);
        return toHealthRecordVO(entity);
    }

    /**
     * 对创建请求应用患者资料字段。
     *
     * @param entity 患者实体
     * @param request 创建患者请求
     */
    private void applyPatientProfile(PatPatientEntity entity, CreatePatientRequest request) {
        entity.setName(request.getPatientName());
        entity.setPatientName(request.getPatientName());
        entity.setGender(request.getGender());
        entity.setAge(request.getAge());
        entity.setPhone(request.getPhone());
        entity.setRiskLevel(defaultIfBlank(request.getRiskLevel(), DEFAULT_RISK_LEVEL));
        entity.setIdCard(defaultIfBlank(request.getIdCard(), ""));
        entity.setBirthday(parseDate(request.getBirthday()));
        entity.setAddress(defaultIfBlank(request.getAddress(), ""));
        entity.setLastVisit(parseDate(request.getLastVisit()));
    }

    /**
     * 对更新请求应用患者资料字段。
     *
     * @param entity 患者实体
     * @param request 更新患者资料请求
     */
    private void applyPatientProfile(PatPatientEntity entity, UpdatePatientProfileRequest request) {
        entity.setName(request.getPatientName());
        entity.setPatientName(request.getPatientName());
        entity.setGender(request.getGender());
        entity.setAge(request.getAge());
        entity.setPhone(request.getPhone());
        entity.setRiskLevel(defaultIfBlank(request.getRiskLevel(), DEFAULT_RISK_LEVEL));
        entity.setIdCard(defaultIfBlank(request.getIdCard(), ""));
        entity.setBirthday(parseDate(request.getBirthday()));
        entity.setAddress(defaultIfBlank(request.getAddress(), ""));
        entity.setLastVisit(parseDate(request.getLastVisit()));
    }

    /**
     * 构造患者激活查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<PatPatientEntity> activePatientWrapper() {
        return new LambdaQueryWrapper<PatPatientEntity>();
    }

    /**
     * 构造健康档案激活查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<PatHealthRecordEntity> activeHealthRecordWrapper() {
        return new LambdaQueryWrapper<PatHealthRecordEntity>();
    }

    /**
     * 校验实体存在。
     *
     * @param entity 实体对象
     * @param message 错误消息
     * @param <T> 实体类型
     * @return 非空实体
     */
    private <T> T requireEntity(T entity, String message) {
        if (entity == null) {
            throw new BizException(404, message);
        }
        return entity;
    }

    /**
     * 校验当前请求处于有效业务租户上下文。
     *
     * @param message 不满足条件时的错误消息
     */
    private void ensureBusinessTenantContext(String message) {
        Long tenantId = TokenPrincipalContext.get().getTenantId();
        if (tenantId == null || tenantId <= 0L || TokenPrincipalContext.get().getPlatformRequest()) {
            throw new BizException(403, message);
        }
    }

    /**
     * 查询当前租户下默认患者（不存在时自动创建）。
     *
     * @return 患者实体
     */
    private PatPatientEntity getOrCreateCurrentPatient() {
        String loginUserId = TokenPrincipalContext.get().getBusinessUserId();
        Long tenantId = TokenPrincipalContext.get().getTenantId();
        if (loginUserId == null || loginUserId.isBlank() || tenantId == null || tenantId <= 0L) {
            log.warn("查询当前患者档案失败，登录用户或租户编号无效");
            throw new BizException(401, "当前登录用户无效");
        }
        log.info("查询或创建当前患者档案，userId={}，tenantId={}", loginUserId, tenantId);
        PatPatientEntity existing = patPatientMapper.selectOne(new LambdaQueryWrapper<PatPatientEntity>()
            .eq(PatPatientEntity::getUserId, loginUserId)
            .last("limit 1"));
        if (existing != null) {
            return existing;
        }
        // 不存在则自动创建
        PatPatientEntity entity = new PatPatientEntity();
        entity.setTenantId(tenantId);
        entity.setUserId(loginUserId);
        entity.setPhone("");
        entity.setPatientName("");
        entity.setName("");
        patPatientMapper.insert(entity);
        log.info("患者档案自动创建成功，patientId={}，userId={}", entity.getId(), loginUserId);
        return entity;
    }

    /**
     * 查询当前租户下默认患者（不存在时抛出异常）。
     *
     * @return 患者实体
     */
    private PatPatientEntity requireCurrentPatient() {
        String loginUserId = TokenPrincipalContext.get().getBusinessUserId();
        if (loginUserId == null || loginUserId.isBlank()) {
            log.warn("查询当前患者档案失败，登录用户编号为空");
            throw new BizException(401, "当前登录用户无效");
        }
        log.info("按登录账号查询当前患者档案，userId={}", loginUserId);
        return requireEntity(patPatientMapper.selectOne(activePatientWrapper()
            .eq(PatPatientEntity::getUserId, loginUserId)
            .last("limit 1")), "当前登录账号未关联患者档案");
    }

    /**
     * 查询指定患者。
     *
     * @param id 患者编号
     * @return 患者实体
     */
    private PatPatientEntity requireActivePatient(Long id) {
        return requireEntity(patPatientMapper.selectOne(new LambdaQueryWrapper<PatPatientEntity>()
            .eq(PatPatientEntity::getId, id)
            .last("limit 1")), "患者档案不存在");
    }

    /**
     * 转换患者展示对象。
     *
     * @param entity 患者实体
     * @return 患者展示对象
     */
    private PatientProfileVO toPatientProfileVO(PatPatientEntity entity) {
        return toPatientProfileVO(entity, countHealthRecords(entity.getId()), resolveLatestRecord(entity.getId()));
    }

    /**
     * 转换患者展示对象。
     *
     * @param entity 患者实体
     * @param healthRecordCount 健康档案数量
     * @param latestRecord 最新健康档案
     * @return 患者展示对象
     */
    private PatientProfileVO toPatientProfileVO(PatPatientEntity entity, Integer healthRecordCount, PatHealthRecordEntity latestRecord) {
        PatientProfileVO vo = new PatientProfileVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setPatientName(resolvePatientName(entity));
        vo.setPhone(defaultIfBlank(entity.getPhone(), ""));
        vo.setMaskedPhone(maskPhone(entity.getPhone()));
        vo.setGender(defaultIfBlank(entity.getGender(), ""));
        vo.setAge(defaultInt(entity.getAge(), 0));
        vo.setRiskLevel(defaultIfBlank(entity.getRiskLevel(), DEFAULT_RISK_LEVEL));
        vo.setIdCard(defaultIfBlank(entity.getIdCard(), ""));
        vo.setBirthday(formatDate(entity.getBirthday()));
        vo.setAddress(defaultIfBlank(entity.getAddress(), ""));
        vo.setLastVisit(formatDate(entity.getLastVisit()));
        vo.setHealthRecordCount(defaultInt(healthRecordCount, 0));
        vo.setLatestRecordSummary(latestRecord == null ? "-" : defaultIfBlank(latestRecord.getSummary(), "-"));
        vo.setUpdateTime(formatDateTime(entity.getUpdateTime()));
        return vo;
    }

    /**
     * 转换健康档案展示对象。
     *
     * @param entity 健康档案实体
     * @return 健康档案展示对象
     */
    private HealthRecordVO toHealthRecordVO(PatHealthRecordEntity entity) {
        HealthRecordVO vo = new HealthRecordVO();
        vo.setId(entity.getId());
        vo.setPatientId(entity.getPatientId());
        vo.setTitle(defaultIfBlank(entity.getTitle(), ""));
        vo.setSummary(defaultIfBlank(entity.getSummary(), ""));
        vo.setAllergies(defaultIfBlank(entity.getAllergies(), ""));
        vo.setHistory(defaultIfBlank(entity.getHistory(), ""));
        vo.setDiagnosis(defaultIfBlank(entity.getDiagnosis(), ""));
        vo.setRemark(defaultIfBlank(entity.getRemark(), ""));
        vo.setCreateTime(formatDateTime(entity.getCreateTime()));
        return vo;
    }

    /**
     * 统计患者健康档案数量。
     *
     * @param patientId 患者编号
     * @return 健康档案数量
     */
    private int countHealthRecords(Long patientId) {
        return Math.toIntExact(patHealthRecordMapper.selectCount(new LambdaQueryWrapper<PatHealthRecordEntity>()
            .eq(PatHealthRecordEntity::getPatientId, patientId)));
    }

    /**
     * 读取患者最新档案摘要。
     *
     * @param patientId 患者编号
     * @return 最新摘要
     */
    private PatHealthRecordEntity resolveLatestRecord(Long patientId) {
        return patHealthRecordMapper.selectOne(new LambdaQueryWrapper<PatHealthRecordEntity>()
            .eq(PatHealthRecordEntity::getPatientId, patientId)
            .orderByDesc(PatHealthRecordEntity::getId)
            .last("limit 1"));
    }

    /**
     * 解析患者姓名。
     *
     * @param entity 患者实体
     * @return 患者姓名
     */
    private String resolvePatientName(PatPatientEntity entity) {
        return defaultIfBlank(entity.getPatientName(), defaultIfBlank(entity.getName(), ""));
    }

    /**
     * 手机号脱敏。
     *
     * @param phone 手机号
     * @return 脱敏手机号
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return defaultIfBlank(phone, "");
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 解析日期字符串。
     *
     * @param value 日期字符串
     * @return 日期对象
     */
    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BizException(400, "日期格式必须为 yyyy-MM-dd");
        }
    }

    /**
     * 格式化日期。
     *
     * @param value 日期对象
     * @return 日期字符串
     */
    private String formatDate(LocalDate value) {
        return value == null ? "" : value.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期时间。
     *
     * @param value 日期时间对象
     * @return 日期时间字符串
     */
    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMATTER);
    }

    /**
     * 设置默认字符串值。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 设置默认长整型值。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的长整型值
     */
    private Long defaultLong(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 设置默认整型值。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的整型值
     */
    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}
