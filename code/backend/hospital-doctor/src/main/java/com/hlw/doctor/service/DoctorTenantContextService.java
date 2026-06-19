package com.hlw.doctor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.doctor.dto.BindDoctorDepartmentRequest;
import com.hlw.doctor.dto.CreateDepartmentRequest;
import com.hlw.doctor.dto.CreateDoctorRequest;
import com.hlw.doctor.dto.CreateScheduleRequest;
import com.hlw.doctor.dto.UpdateDoctorStatusRequest;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 科室数据访问组件。 */
    private final DocDepartmentMapper docDepartmentMapper;
    /** 医生数据访问组件。 */
    private final DocDoctorMapper docDoctorMapper;
    /** 医生科室关系数据访问组件。 */
    private final DocDoctorDepartmentMapper docDoctorDepartmentMapper;
    /** 排班数据访问组件。 */
    private final DocScheduleMapper docScheduleMapper;

    /**
     * 查询科室列表。
     *
     * @return 科室展示列表
     */
    public List<DepartmentVO> listDepartments() {
        log.info("查询科室列表");
        Map<Long, Long> departmentDoctorCount = docDoctorDepartmentMapper.selectList(activeDoctorDepartmentWrapper())
            .stream()
            .collect(Collectors.groupingBy(DocDoctorDepartmentEntity::getDepartmentId, Collectors.mapping(DocDoctorDepartmentEntity::getDoctorId, Collectors.collectingAndThen(Collectors.toSet(), value -> (long) value.size()))));
        return docDepartmentMapper.selectList(activeDepartmentWrapper())
            .stream()
            .sorted(Comparator.comparing(DocDepartmentEntity::getSort).thenComparing(DocDepartmentEntity::getId))
            .map(entity -> toDepartmentVO(entity, departmentDoctorCount.get(entity.getId())))
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
        ensureBusinessTenantContext("医生模块操作缺少有效租户上下文");
        log.info("创建科室，name={}，parentId={}", request.getName(), request.getParentId());
        DocDepartmentEntity entity = new DocDepartmentEntity();
        entity.setName(request.getName());
        entity.setDepartmentName(request.getName());
        entity.setDoctorCount(0);
        entity.setQueueDesc(defaultIfBlank(request.getQueue(), "当前等候 0 人"));
        entity.setParentId(defaultLong(request.getParentId(), 0L));
        entity.setSort(defaultInt(request.getSort(), 0));
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        entity.setDescription(defaultIfBlank(request.getDescription(), ""));
        docDepartmentMapper.insert(entity);
        return toDepartmentVO(entity, 0L);
    }

    /**
     * 查询医生列表。
     *
     * @return 医生展示列表
     */
    public List<DoctorVO> listDoctors() {
        log.info("查询医生列表");
        return docDoctorMapper.selectList(activeDoctorWrapper())
            .stream()
            .sorted(Comparator.comparing(DocDoctorEntity::getId))
            .map(this::toDoctorVO)
            .toList();
    }

    /**
     * 查询医生详情。
     *
     * @param id 医生编号
     * @return 医生展示对象
     */
    public DoctorVO getDoctor(Long id) {
        log.info("查询医生详情，doctorId={}", id);
        return toDoctorVO(requireActiveDoctor(id));
    }

    /**
     * 创建医生。
     *
     * @param request 创建医生请求
     * @return 医生展示对象
     */
    @Transactional
    public DoctorVO createDoctor(CreateDoctorRequest request) {
        ensureBusinessTenantContext("医生模块操作缺少有效租户上下文");
        log.info("创建医生，name={}，department={}，title={}", request.getName(), request.getDepartment(), request.getTitle());
        DocDoctorEntity entity = new DocDoctorEntity();
        entity.setUserId(defaultLong(request.getUserId(), 0L));
        entity.setName(request.getName());
        entity.setDoctorName(request.getName());
        entity.setTitle(request.getTitle());
        entity.setDepartment(request.getDepartment());
        entity.setSpecialty(defaultIfBlank(request.getSpecialty(), DEFAULT_SPECIALTY));
        entity.setConsultFee(defaultDecimal(request.getConsultFee(), BigDecimal.ZERO));
        entity.setConsultStatus(defaultIfBlank(request.getConsultStatus(), DEFAULT_CONSULT_STATUS));
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_DOCTOR_STATUS));
        entity.setScheduleDesc(defaultIfBlank(request.getSchedule(), DEFAULT_SCHEDULE));
        entity.setPatientCount(0);
        docDoctorMapper.insert(entity);
        return toDoctorVO(entity);
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
        ensureBusinessTenantContext("医生模块操作缺少有效租户上下文");
        log.info("更新医生状态，doctorId={}，status={}", id, request.getStatus());
        DocDoctorEntity entity = requireActiveDoctor(id);
        entity.setConsultStatus(request.getStatus());
        entity.setStatus(resolveDoctorDisplayStatus(request.getStatus()));
        docDoctorMapper.updateById(entity);
        return toDoctorVO(entity);
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
        log.info("绑定医生科室，doctorId={}，departmentId={}", doctorId, request.getDepartmentId());
        requireActiveDoctor(doctorId);
        requireActiveDepartment(request.getDepartmentId());
        DocDoctorDepartmentEntity relation = docDoctorDepartmentMapper.selectOne(new LambdaQueryWrapper<DocDoctorDepartmentEntity>()
            .eq(DocDoctorDepartmentEntity::getDoctorId, doctorId)
            .eq(DocDoctorDepartmentEntity::getDepartmentId, request.getDepartmentId())
            .last("limit 1"));
        if (relation == null) {
            relation = new DocDoctorDepartmentEntity();
            relation.setDoctorId(doctorId);
            relation.setDepartmentId(request.getDepartmentId());
            relation.setIsFree(boolToInt(request.getFree(), 0));
            relation.setAppointmentFee(defaultDecimal(request.getAppointmentFee(), BigDecimal.ZERO));
            docDoctorDepartmentMapper.insert(relation);
        } else {
            relation.setIsFree(boolToInt(request.getFree(), relation.getIsFree()));
            relation.setAppointmentFee(defaultDecimal(request.getAppointmentFee(), relation.getAppointmentFee()));
            docDoctorDepartmentMapper.updateById(relation);
        }
        refreshDepartmentDoctorCount(request.getDepartmentId());
        return toDoctorDepartmentBindingVO(relation);
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
     * @param departmentId 科室编号
     */
    private void refreshDepartmentDoctorCount(Long departmentId) {
        DocDepartmentEntity department = requireActiveDepartment(departmentId);
        int doctorCount = Math.toIntExact(docDoctorDepartmentMapper.selectCount(new LambdaQueryWrapper<DocDoctorDepartmentEntity>()
            .eq(DocDoctorDepartmentEntity::getDepartmentId, departmentId)));
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
        Long tenantId = TokenPrincipalContext.get().getTenantId();
        if (tenantId == null || tenantId <= 0L || TokenPrincipalContext.get().getPlatformRequest()) {
            throw new BizException(403, message);
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
            .eq(DocDepartmentEntity::getId, id)
            .last("limit 1")), "科室不存在");
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
        vo.setId(entity.getId());
        vo.setName(defaultIfBlank(entity.getDepartmentName(), entity.getName()));
        vo.setDoctorCount(relationDoctorCount == null || relationDoctorCount == 0L ? defaultInt(entity.getDoctorCount(), 0) : relationDoctorCount.intValue());
        vo.setQueue(defaultIfBlank(entity.getQueueDesc(), "当前等候 0 人"));
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 转换医生展示对象。
     *
     * @param entity 医生实体
     * @return 医生展示对象
     */
    private DoctorVO toDoctorVO(DocDoctorEntity entity) {
        DoctorVO vo = new DoctorVO();
        vo.setId(entity.getId());
        vo.setName(defaultIfBlank(entity.getDoctorName(), entity.getName()));
        vo.setTitle(entity.getTitle());
        vo.setDepartment(entity.getDepartment());
        vo.setSpecialty(entity.getSpecialty());
        vo.setStatus(entity.getStatus());
        vo.setConsultStatus(entity.getConsultStatus());
        vo.setSchedule(entity.getScheduleDesc());
        vo.setPatientCount(defaultInt(entity.getPatientCount(), 0));
        vo.setConsultFee(defaultDecimal(entity.getConsultFee(), BigDecimal.ZERO).stripTrailingZeros().toPlainString());
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
        vo.setDepartmentId(entity.getDepartmentId());
        vo.setFree(Objects.equals(entity.getIsFree(), 1));
        vo.setAppointmentFee(defaultDecimal(entity.getAppointmentFee(), BigDecimal.ZERO));
        return vo;
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
