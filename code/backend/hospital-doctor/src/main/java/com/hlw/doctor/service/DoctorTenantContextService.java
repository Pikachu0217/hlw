package com.hlw.doctor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.resp.InternalDeptResp;
import com.hlw.common.core.domain.system.resp.InternalUserResp;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.security.TokenPrincipal;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.doctor.client.SystemDeptFeignClient;
import com.hlw.doctor.client.SystemUserFeignClient;
import com.hlw.doctor.dto.BindDoctorDepartmentRequest;
import com.hlw.doctor.dto.CreateDepartmentRequest;
import com.hlw.doctor.dto.CreateDoctorRequest;
import com.hlw.doctor.dto.CreateScheduleRequest;
import com.hlw.doctor.dto.UpdateDoctorStatusRequest;
import com.hlw.doctor.domain.resp.InternalDoctorResp;
import com.hlw.doctor.entity.DocDepartmentEntity;
import com.hlw.doctor.entity.DocDoctorDepartmentEntity;
import com.hlw.doctor.entity.DocDoctorEntity;
import com.hlw.doctor.entity.DocScheduleEntity;
import com.hlw.doctor.mapper.DocDepartmentMapper;
import com.hlw.doctor.mapper.DocDoctorDepartmentMapper;
import com.hlw.doctor.mapper.DocDoctorMapper;
import com.hlw.doctor.mapper.DocScheduleMapper;
import com.hlw.doctor.vo.DepartmentVO;
import com.hlw.doctor.vo.DoctorDepartmentBindingVO;
import com.hlw.doctor.vo.DoctorVO;
import com.hlw.doctor.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 医生模块租户上下文服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorTenantContextService {
    private static final String DEFAULT_STATUS = "启用";
    private static final String DEFAULT_DOCTOR_STATUS = "接诊中";
    private static final String DEFAULT_CONSULT_STATUS = "ONLINE";
    private static final String DEFAULT_SPECIALTY = "全科诊疗";
    private static final String DEFAULT_SCHEDULE = "待排班";
    private static final String DOCTOR_USER_TYPE = "doctor";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 科室数据访问组件。 */
    private final DocDepartmentMapper docDepartmentMapper;
    /** 医生数据访问组件。 */
    private final DocDoctorMapper docDoctorMapper;
    /** 医生科室关系数据访问组件。 */
    private final DocDoctorDepartmentMapper docDoctorDepartmentMapper;
    /** 排班数据访问组件。 */
    private final DocScheduleMapper docScheduleMapper;
    /** 系统用户内部客户端。 */
    private final SystemUserFeignClient systemUserFeignClient;
    /** 系统部门内部客户端。 */
    private final SystemDeptFeignClient systemDeptFeignClient;

    /**
     * 查询科室列表。
     *
     * @return 科室展示列表
     */
    public List<DepartmentVO> listDepartments() {
        Long tenantId = currentBusinessTenantId("查询科室资源缺少有效租户上下文");
        log.info("查询科室资源列表，tenantId={}", tenantId);
        List<InternalDeptResp> departments = readFeignData(systemDeptFeignClient.listDepartments(tenantId), "查询系统科室失败");
        Map<Long, DocDepartmentEntity> extensionMap = docDepartmentMapper.selectList(activeDepartmentWrapper()).stream()
            .filter(entity -> entity.getDeptId() != null)
            .collect(Collectors.toMap(DocDepartmentEntity::getDeptId, Function.identity(), (current, next) -> current));
        Map<Long, Long> departmentDoctorCount = docDoctorDepartmentMapper.selectList(activeDoctorDepartmentWrapper())
            .stream()
            .collect(Collectors.groupingBy(DocDoctorDepartmentEntity::getDeptId, Collectors.mapping(DocDoctorDepartmentEntity::getDoctorId, Collectors.collectingAndThen(Collectors.toSet(), value -> (long) value.size()))));
        return departments.stream()
            .sorted(Comparator.comparing((InternalDeptResp dept) -> defaultInt(dept.getOrderNum(), 0)).thenComparing(InternalDeptResp::getId))
            .map(dept -> toDepartmentVO(dept, extensionMap.get(dept.getId()), departmentDoctorCount.get(dept.getId())))
            .toList();
    }

    /**
     * 创建科室。
     *
     * @param request 创建科室请求
     * @return 科室展示对象
     */
    @Transactional
    public DepartmentVO createDepartment(CreateDepartmentRequest request) {
        return updateDepartmentExtension(request.getDeptId(), request);
    }

    /**
     * 更新科室扩展属性。
     *
     * @param deptId 系统部门编号
     * @param request 科室扩展请求
     * @return 科室展示对象
     */
    @Transactional
    public DepartmentVO updateDepartmentExtension(Long deptId, CreateDepartmentRequest request) {
        Long tenantId = currentBusinessTenantId("医生模块操作缺少有效租户上下文");
        if (deptId == null || deptId <= 0L) {
            throw new BizException(400, "系统部门编号不能为空");
        }
        log.info("更新科室扩展属性，tenantId={}，deptId={}，name={}", tenantId, deptId, request.getName());
        InternalDeptResp dept = readFeignData(systemDeptFeignClient.detail(deptId, tenantId), "查询系统科室失败");
        if (dept == null || !Objects.equals(dept.getIsDepartment(), 1)) {
            log.warn("科室扩展属性更新失败，系统部门不是科室，tenantId={}，deptId={}", tenantId, deptId);
            throw new BizException(400, "系统部门不是科室");
        }
        DocDepartmentEntity entity = findDepartmentExtensionByDeptId(deptId);
        boolean create = entity == null;
        if (create) {
            entity = new DocDepartmentEntity();
            entity.setDeptId(deptId);
            entity.setTenantId(tenantId);
            entity.setDoctorCount(0);
        }
        fillDepartmentExtension(entity, dept, request);
        if (create) {
            docDepartmentMapper.insert(entity);
        } else {
            docDepartmentMapper.updateById(entity);
        }
        Long doctorCount = docDoctorDepartmentMapper.selectCount(new LambdaQueryWrapper<DocDoctorDepartmentEntity>()
            .eq(DocDoctorDepartmentEntity::getDeptId, deptId));
        return toDepartmentVO(dept, entity, doctorCount);
    }

    /**
     * 查询医生列表。
     *
     * @return 医生展示列表
     */
    public List<DoctorVO> listDoctors() {
        Long tenantId = currentBusinessTenantId("查询医生资源缺少有效租户上下文");
        log.info("查询医生资源列表，tenantId={}", tenantId);
        List<InternalUserResp> doctorUsers = readFeignData(systemUserFeignClient.listByUserType(tenantId, DOCTOR_USER_TYPE), "查询医生账号失败");
        Map<Long, DocDoctorEntity> extensionMap = docDoctorMapper.selectList(activeDoctorWrapper()).stream()
            .collect(Collectors.toMap(DocDoctorEntity::getUserId, Function.identity(), (current, next) -> current));
        return doctorUsers.stream()
            .sorted(Comparator.comparing(InternalUserResp::getId))
            .map(user -> toDoctorVO(user, extensionMap.get(user.getId())))
            .toList();
    }

    /**
     * 查询医生详情。
     *
     * @param id 医生编号
     * @return 医生展示对象
     */
    public DoctorVO getDoctor(Long id) {
        Long tenantId = currentBusinessTenantId("查询医生详情缺少有效租户上下文");
        log.info("查询医生资源详情，tenantId={}，userId={}", tenantId, id);
        InternalUserResp user = readFeignData(systemUserFeignClient.listByUserType(tenantId, DOCTOR_USER_TYPE), "查询医生账号失败")
            .stream()
            .filter(item -> Objects.equals(item.getId(), id))
            .findFirst()
            .orElseThrow(() -> new BizException(404, "医生账号不存在"));
        return toDoctorVO(user, findDoctorExtensionByUserId(id));
    }

    /**
     * 创建医生。
     *
     * @param request 创建医生请求
     * @return 医生展示对象
     */
    @Transactional
    public DoctorVO createDoctor(CreateDoctorRequest request) {
        return updateDoctorExtension(request.getUserId(), request);
    }

    /**
     * 更新医生扩展属性。
     *
     * @param userId 医生账号编号
     * @param request 医生扩展请求
     * @return 医生展示对象
     */
    @Transactional
    public DoctorVO updateDoctorExtension(Long userId, CreateDoctorRequest request) {
        Long tenantId = currentBusinessTenantId("医生模块操作缺少有效租户上下文");
        if (userId == null || userId <= 0L) {
            throw new BizException(400, "医生账号编号不能为空");
        }
        log.info("更新医生扩展属性，tenantId={}，userId={}，title={}", tenantId, userId, request.getTitle());
        InternalUserResp user = readFeignData(systemUserFeignClient.listByUserType(tenantId, DOCTOR_USER_TYPE), "查询医生账号失败")
            .stream()
            .filter(item -> Objects.equals(item.getId(), userId))
            .findFirst()
            .orElseThrow(() -> new BizException(404, "医生账号不存在"));
        DocDoctorEntity entity = findDoctorExtensionByUserId(userId);
        boolean create = entity == null;
        if (create) {
            entity = new DocDoctorEntity();
            entity.setUserId(userId);
            entity.setTenantId(tenantId);
            entity.setPatientCount(0);
        }
        fillDoctorExtension(entity, user, request);
        if (create) {
            docDoctorMapper.insert(entity);
        } else {
            docDoctorMapper.updateById(entity);
        }
        return toDoctorVO(user, entity);
    }

    /**
     * 更新医生状态。
     *
     * @param id 医生编号
     * @param request 状态请求
     * @return 医生展示对象
     */
    @Transactional
    public DoctorVO updateDoctorStatus(Long id, UpdateDoctorStatusRequest request) {
        CreateDoctorRequest extensionRequest = new CreateDoctorRequest();
        extensionRequest.setUserId(id);
        extensionRequest.setConsultStatus(request.getStatus());
        extensionRequest.setStatus(resolveDoctorDisplayStatus(request.getStatus()));
        return updateDoctorExtension(id, extensionRequest);
    }

    /**
     * 绑定医生科室。
     *
     * @param doctorId 医生编号
     * @param request 绑定请求
     * @return 绑定展示对象
     */
    @Transactional
    public DoctorDepartmentBindingVO bindDoctorDepartment(Long doctorId, BindDoctorDepartmentRequest request) {
        ensureBusinessTenantContext("医生模块操作缺少有效租户上下文");
        log.info("绑定医生科室，doctorId={}，deptId={}", doctorId, request.getDeptId());
        requireActiveDoctor(doctorId);
        requireActiveDepartment(request.getDeptId());
        DocDoctorDepartmentEntity relation = docDoctorDepartmentMapper.selectOne(new LambdaQueryWrapper<DocDoctorDepartmentEntity>()
            .eq(DocDoctorDepartmentEntity::getDoctorId, doctorId)
            .eq(DocDoctorDepartmentEntity::getDeptId, request.getDeptId())
            .last("limit 1"));
        if (relation == null) {
            relation = new DocDoctorDepartmentEntity();
            relation.setDoctorId(doctorId);
            relation.setDeptId(request.getDeptId());
            relation.setIsFree(boolToInt(request.getFree(), 0));
            relation.setAppointmentFee(defaultDecimal(request.getAppointmentFee(), BigDecimal.ZERO));
            docDoctorDepartmentMapper.insert(relation);
        } else {
            relation.setIsFree(boolToInt(request.getFree(), relation.getIsFree()));
            relation.setAppointmentFee(defaultDecimal(request.getAppointmentFee(), relation.getAppointmentFee()));
            docDoctorDepartmentMapper.updateById(relation);
        }
        refreshDepartmentDoctorCount(request.getDeptId());
        return toDoctorDepartmentBindingVO(relation);
    }

    /**
     * 按租户和登录用户查询内部医生档案。
     *
     * @param tenantId 租户编号
     * @param userId 登录用户编号
     * @return 内部医生档案
     */
    public InternalDoctorResp getInternalDoctorByUser(Long tenantId, Long userId) {
        log.info("按登录用户查询内部医生档案，tenantId={}，userId={}", tenantId, userId);
        if (tenantId == null || tenantId < 0L || userId == null || userId <= 0L) {
            log.warn("查询内部医生档案失败，租户或用户编号无效，tenantId={}，userId={}", tenantId, userId);
            throw new BizException(400, "租户或用户编号无效");
        }
        DocDoctorEntity entity = docDoctorMapper.selectOne(new LambdaQueryWrapper<DocDoctorEntity>()
            .eq(DocDoctorEntity::getTenantId, tenantId)
            .eq(DocDoctorEntity::getUserId, userId)
            .eq(DocDoctorEntity::getDeleted, 0)
            .last("limit 1"));
        if (entity == null) {
            log.warn("登录用户未绑定医生档案，tenantId={}，userId={}", tenantId, userId);
            throw new BizException(403, "当前登录账号未绑定医生档案");
        }
        return new InternalDoctorResp(entity.getId(), entity.getUserId(), entity.getTenantId(), resolveDoctorName(entity));
    }

    /**
     * 查询排班列表。
     *
     * @return 排班展示列表
     */
    public List<ScheduleVO> listSchedules() {
        log.info("查询排班列表");
        Map<Long, String> doctorNameMap = docDoctorMapper.selectList(activeDoctorWrapper())
            .stream()
            .collect(Collectors.toMap(DocDoctorEntity::getId, doctor -> defaultIfBlank(doctor.getDoctorName(), doctor.getName())));
        return docScheduleMapper.selectList(activeScheduleWrapper())
            .stream()
            .sorted(Comparator.comparing(DocScheduleEntity::getId))
            .map(entity -> toScheduleVO(entity, doctorNameMap.get(entity.getDoctorId())))
            .toList();
    }

    /**
     * 创建排班。
     *
     * @param request 创建排班请求
     * @return 排班展示对象
     */
    @Transactional
    public ScheduleVO createSchedule(CreateScheduleRequest request) {
        ensureBusinessTenantContext("医生模块操作缺少有效租户上下文");
        log.info("创建医生排班，doctorId={}，slot={}，scheduleDate={}",
            request.getDoctorId(), request.getSlot(), request.getScheduleDate());
        DocDoctorEntity doctor = requireActiveDoctor(request.getDoctorId());
        DocScheduleEntity entity = new DocScheduleEntity();
        entity.setDoctorId(request.getDoctorId());
        entity.setSlot(request.getSlot());
        entity.setScheduleDate(parseDate(defaultIfBlank(request.getScheduleDate(), LocalDate.now().format(DATE_FORMATTER))));
        entity.setTimeSlot(defaultIfBlank(request.getTimeSlot(), request.getSlot()));
        entity.setTotalNumber(defaultInt(request.getTotalNumber(), 30));
        entity.setRemainNumber(defaultInt(request.getRemainNumber(), entity.getTotalNumber()));
        docScheduleMapper.insert(entity);
        doctor.setScheduleDesc(entity.getSlot());
        docDoctorMapper.updateById(doctor);
        return toScheduleVO(entity, defaultIfBlank(doctor.getDoctorName(), doctor.getName()));
    }

    /**
     * 构造科室激活查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<DocDepartmentEntity> activeDepartmentWrapper() {
        return new LambdaQueryWrapper<DocDepartmentEntity>();
    }

    /**
     * 构造医生激活查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<DocDoctorEntity> activeDoctorWrapper() {
        return new LambdaQueryWrapper<DocDoctorEntity>();
    }

    /**
     * 构造医生科室关系激活查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<DocDoctorDepartmentEntity> activeDoctorDepartmentWrapper() {
        return new LambdaQueryWrapper<DocDoctorDepartmentEntity>();
    }

    /**
     * 构造排班激活查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<DocScheduleEntity> activeScheduleWrapper() {
        return new LambdaQueryWrapper<DocScheduleEntity>();
    }

    /**
     * 刷新科室医生数量。
     *
     * @param deptId 科室编号
     */
    private void refreshDepartmentDoctorCount(Long deptId) {
        DocDepartmentEntity department = requireActiveDepartment(deptId);
        int doctorCount = Math.toIntExact(docDoctorDepartmentMapper.selectCount(new LambdaQueryWrapper<DocDoctorDepartmentEntity>()
            .eq(DocDoctorDepartmentEntity::getDeptId, deptId)));
        department.setDoctorCount(doctorCount);
        docDepartmentMapper.updateById(department);
    }

    /**
     * 校验实体存在。
     *
     * @param entity 实体
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
        TokenPrincipal principal = TokenPrincipalContext.get();
        Long tenantId = principal == null ? null : principal.getTenantId();
        if (tenantId == null || tenantId < 0L) {
            log.warn("医生模块租户上下文无效，tenantId={}，platformRequest={}",
                tenantId, principal == null ? null : principal.getPlatformRequest());
            throw new BizException(403, message);
        }
        if (Boolean.TRUE.equals(principal.getPlatformRequest())) {
            log.info("医生模块使用平台默认租户上下文，tenantId={}", tenantId);
        }
    }

    /**
     * 校验医生存在。
     *
     * @param id 医生编号
     * @return 医生实体
     */
    private DocDoctorEntity requireActiveDoctor(Long id) {
        return requireEntity(docDoctorMapper.selectOne(new LambdaQueryWrapper<DocDoctorEntity>()
            .eq(DocDoctorEntity::getId, id)
            .last("limit 1")), "医生不存在");
    }

    /**
     * 校验科室存在。
     *
     * @param id 科室编号
     * @return 科室实体
     */
    private DocDepartmentEntity requireActiveDepartment(Long id) {
        return requireEntity(docDepartmentMapper.selectOne(new LambdaQueryWrapper<DocDepartmentEntity>()
            .and(wrapper -> wrapper.eq(DocDepartmentEntity::getDeptId, id).or().eq(DocDepartmentEntity::getId, id))
            .last("limit 1")), "科室不存在");
    }

    /**
     * 按系统部门编号查询科室扩展信息。
     *
     * @param deptId 系统部门编号
     * @return 科室扩展信息
     */
    private DocDepartmentEntity findDepartmentExtensionByDeptId(Long deptId) {
        if (deptId == null) {
            return null;
        }
        return docDepartmentMapper.selectOne(new LambdaQueryWrapper<DocDepartmentEntity>()
            .eq(DocDepartmentEntity::getDeptId, deptId)
            .last("limit 1"));
    }

    /**
     * 按账号编号查询医生扩展信息。
     *
     * @param userId 医生账号编号
     * @return 医生扩展信息
     */
    private DocDoctorEntity findDoctorExtensionByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return docDoctorMapper.selectOne(new LambdaQueryWrapper<DocDoctorEntity>()
            .eq(DocDoctorEntity::getUserId, userId)
            .last("limit 1"));
    }

    /**
     * 填充科室扩展信息。
     *
     * @param entity 科室扩展实体
     * @param dept 系统部门
     * @param request 科室扩展请求
     */
    private void fillDepartmentExtension(DocDepartmentEntity entity, InternalDeptResp dept, CreateDepartmentRequest request) {
        entity.setName(defaultIfBlank(request.getName(), dept.getDeptName()));
        entity.setDepartmentName(defaultIfBlank(request.getName(), dept.getDeptName()));
        entity.setParentId(defaultLong(dept.getParentId(), 0L));
        entity.setSort(defaultInt(request.getSort(), defaultInt(dept.getOrderNum(), 0)));
        entity.setQueueDesc(defaultIfBlank(request.getQueue(), "当前等候 0 人"));
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        entity.setDescription(defaultIfBlank(request.getDescription(), ""));
    }

    /**
     * 填充医生扩展信息。
     *
     * @param entity 医生扩展实体
     * @param user 系统医生账号
     * @param request 医生扩展请求
     */
    private void fillDoctorExtension(DocDoctorEntity entity, InternalUserResp user, CreateDoctorRequest request) {
        String displayName = defaultIfBlank(request.getName(), user.getRealName());
        entity.setName(displayName);
        entity.setDoctorName(displayName);
        entity.setTitle(defaultIfBlank(request.getTitle(), defaultIfBlank(entity.getTitle(), "医师")));
        entity.setDepartment(defaultIfBlank(request.getDepartment(), defaultIfBlank(entity.getDepartment(), user.getDeptName())));
        entity.setSpecialty(defaultIfBlank(request.getSpecialty(), defaultIfBlank(entity.getSpecialty(), DEFAULT_SPECIALTY)));
        entity.setConsultFee(defaultDecimal(request.getConsultFee(), defaultDecimal(entity.getConsultFee(), BigDecimal.ZERO)));
        entity.setConsultStatus(defaultIfBlank(request.getConsultStatus(), defaultIfBlank(entity.getConsultStatus(), DEFAULT_CONSULT_STATUS)));
        entity.setStatus(defaultIfBlank(request.getStatus(), resolveDoctorDisplayStatus(entity.getConsultStatus())));
        entity.setScheduleDesc(defaultIfBlank(request.getSchedule(), defaultIfBlank(entity.getScheduleDesc(), DEFAULT_SCHEDULE)));
        entity.setPatientCount(defaultInt(entity.getPatientCount(), 0));
    }

    /**
     * 读取 Feign 响应数据。
     *
     * @param response Feign 响应
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应数据
     */
    private <T> T readFeignData(R<T> response, String message) {
        if (response == null || response.code() != 200) {
            log.warn("内部服务调用失败，message={}，responseCode={}", message, response == null ? null : response.code());
            throw new BizException(500, message);
        }
        return response.data();
    }

    /**
     * 获取当前业务租户编号。
     *
     * @param message 无效时错误消息
     * @return 租户编号
     */
    private Long currentBusinessTenantId(String message) {
        TokenPrincipal principal = TokenPrincipalContext.get();
        Long tenantId = principal == null ? null : principal.getTenantId();
        if (tenantId == null || tenantId < 0L) {
            log.warn("医生资源租户上下文无效，tenantId={}，platformRequest={}",
                tenantId, principal == null ? null : principal.getPlatformRequest());
            throw new BizException(403, message);
        }
        if (Boolean.TRUE.equals(principal.getPlatformRequest())) {
            log.info("医生资源使用平台默认租户上下文，tenantId={}", tenantId);
        }
        return tenantId;
    }

    /**
     * 解析医生展示状态。
     *
     * @param consultStatus 接诊状态
     * @return 展示状态
     */
    private String resolveDoctorDisplayStatus(String consultStatus) {
        if ("ONLINE".equalsIgnoreCase(consultStatus)) {
            return "接诊中";
        }
        if ("OFFLINE".equalsIgnoreCase(consultStatus)) {
            return "停诊";
        }
        if ("BUSY".equalsIgnoreCase(consultStatus)) {
            return "候诊";
        }
        return consultStatus;
    }

    /**
     * 解析日期字符串。
     *
     * @param value 日期字符串
     * @return 日期对象
     */
    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (Exception exception) {
            throw new BizException(400, "排班日期格式不正确");
        }
    }

    /**
     * 读取默认字符串。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后字符串
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    /**
     * 读取默认整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后整型
     */
    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 读取默认长整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后长整型
     */
    private Long defaultLong(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 读取默认金额。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后金额
     */
    private BigDecimal defaultDecimal(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 布尔值转整型。
     *
     * @param value 布尔值
     * @param defaultValue 默认整型值
     * @return 数值结果
     */
    private Integer boolToInt(Boolean value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    /**
     * 转换科室展示对象。
     *
     * @param entity 科室实体
     * @param relationDoctorCount 关系表医生数量
     * @return 科室展示对象
     */
    private DepartmentVO toDepartmentVO(DocDepartmentEntity entity, Long relationDoctorCount) {
        DepartmentVO vo = new DepartmentVO();
        vo.setId(resolveDeptId(entity));
        vo.setDeptId(resolveDeptId(entity));
        vo.setDepartmentId(entity.getId());
        vo.setName(defaultIfBlank(entity.getDepartmentName(), entity.getName()));
        vo.setDoctorCount(relationDoctorCount == null || relationDoctorCount == 0L ? defaultInt(entity.getDoctorCount(), 0) : relationDoctorCount.intValue());
        vo.setQueue(defaultIfBlank(entity.getQueueDesc(), "当前等候 0 人"));
        vo.setStatus(entity.getStatus());
        vo.setConfigured(true);
        return vo;
    }

    /**
     * 转换科室资源展示对象。
     *
     * @param dept 系统部门
     * @param extension 科室扩展信息
     * @param relationDoctorCount 关系表医生数量
     * @return 科室资源展示对象
     */
    private DepartmentVO toDepartmentVO(InternalDeptResp dept, DocDepartmentEntity extension, Long relationDoctorCount) {
        DepartmentVO vo = new DepartmentVO();
        vo.setId(dept.getId());
        vo.setDeptId(dept.getId());
        vo.setDepartmentId(extension == null ? null : extension.getId());
        vo.setName(extension == null ? dept.getDeptName() : defaultIfBlank(extension.getDepartmentName(), dept.getDeptName()));
        vo.setDoctorCount(relationDoctorCount == null ? 0 : relationDoctorCount.intValue());
        vo.setQueue(extension == null ? "当前等候 0 人" : defaultIfBlank(extension.getQueueDesc(), "当前等候 0 人"));
        vo.setStatus(extension == null ? String.valueOf(defaultInt(dept.getStatus(), 0)) : extension.getStatus());
        vo.setConfigured(extension != null);
        return vo;
    }

    /**
     * 解析医生展示姓名。
     *
     * @param entity 医生实体
     * @return 医生展示姓名
     */
    private String resolveDoctorName(DocDoctorEntity entity) {
        return defaultIfBlank(entity.getDoctorName(), entity.getName());
    }

    /**
     * 转换医生展示对象。
     *
     * @param entity 医生实体
     * @return 医生展示对象
     */
    private DoctorVO toDoctorVO(DocDoctorEntity entity) {
        DoctorVO vo = new DoctorVO();
        vo.setId(entity.getUserId());
        vo.setDoctorId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setName(resolveDoctorName(entity));
        vo.setTitle(entity.getTitle());
        vo.setDepartment(entity.getDepartment());
        vo.setSpecialty(entity.getSpecialty());
        vo.setStatus(entity.getStatus());
        vo.setConsultStatus(entity.getConsultStatus());
        vo.setSchedule(entity.getScheduleDesc());
        vo.setPatientCount(defaultInt(entity.getPatientCount(), 0));
        vo.setConsultFee(defaultDecimal(entity.getConsultFee(), BigDecimal.ZERO).stripTrailingZeros().toPlainString());
        vo.setConfigured(true);
        return vo;
    }

    /**
     * 转换医生资源展示对象。
     *
     * @param user 系统医生账号
     * @param extension 医生扩展信息
     * @return 医生资源展示对象
     */
    private DoctorVO toDoctorVO(InternalUserResp user, DocDoctorEntity extension) {
        DoctorVO vo = new DoctorVO();
        vo.setId(user.getId());
        vo.setUserId(user.getId());
        vo.setDoctorId(extension == null ? null : extension.getId());
        vo.setName(extension == null ? user.getRealName() : resolveDoctorName(extension));
        vo.setTitle(extension == null ? "医师" : extension.getTitle());
        vo.setDepartment(extension == null ? defaultIfBlank(user.getDeptName(), "") : extension.getDepartment());
        vo.setSpecialty(extension == null ? DEFAULT_SPECIALTY : extension.getSpecialty());
        vo.setStatus(extension == null ? DEFAULT_DOCTOR_STATUS : extension.getStatus());
        vo.setConsultStatus(extension == null ? DEFAULT_CONSULT_STATUS : extension.getConsultStatus());
        vo.setSchedule(extension == null ? DEFAULT_SCHEDULE : extension.getScheduleDesc());
        vo.setPatientCount(extension == null ? 0 : defaultInt(extension.getPatientCount(), 0));
        vo.setConsultFee(extension == null ? "0" : defaultDecimal(extension.getConsultFee(), BigDecimal.ZERO).stripTrailingZeros().toPlainString());
        vo.setConfigured(extension != null);
        return vo;
    }

    /**
     * 转换医生科室绑定展示对象。
     *
     * @param entity 医生科室关系实体
     * @return 绑定展示对象
     */
    private DoctorDepartmentBindingVO toDoctorDepartmentBindingVO(DocDoctorDepartmentEntity entity) {
        DoctorDepartmentBindingVO vo = new DoctorDepartmentBindingVO();
        vo.setId(entity.getId());
        vo.setDoctorId(entity.getDoctorId());
        vo.setDeptId(entity.getDeptId());
        vo.setFree(Objects.equals(entity.getIsFree(), 1));
        vo.setAppointmentFee(defaultDecimal(entity.getAppointmentFee(), BigDecimal.ZERO));
        return vo;
    }

    /**
     * 解析科室主数据编号。
     *
     * @param entity 科室扩展实体
     * @return 系统部门编号
     */
    private Long resolveDeptId(DocDepartmentEntity entity) {
        return entity.getDeptId() == null ? entity.getId() : entity.getDeptId();
    }

    /**
     * 转换排班展示对象。
     *
     * @param entity 排班实体
     * @param doctorName 医生姓名
     * @return 排班展示对象
     */
    private ScheduleVO toScheduleVO(DocScheduleEntity entity, String doctorName) {
        ScheduleVO vo = new ScheduleVO();
        vo.setId(entity.getId());
        vo.setDoctorId(entity.getDoctorId());
        vo.setDoctorName(defaultIfBlank(doctorName, ""));
        vo.setSlot(entity.getSlot());
        vo.setScheduleDate(entity.getScheduleDate() == null ? "" : entity.getScheduleDate().format(DATE_FORMATTER));
        vo.setTimeSlot(entity.getTimeSlot());
        vo.setTotalNumber(defaultInt(entity.getTotalNumber(), 0));
        vo.setRemain(defaultInt(entity.getRemainNumber(), 0));
        return vo;
    }
}
