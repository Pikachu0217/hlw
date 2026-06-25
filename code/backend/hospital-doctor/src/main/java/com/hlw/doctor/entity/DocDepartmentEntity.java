package com.hlw.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 科室扩展信息持久化对象。
 */
@Getter
@Setter
@TableName("doc_department")
public class DocDepartmentEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 系统部门编号。 */
    private Long deptId;
    /** 租户编号。 */
    private Long tenantId;
    /** 兼容旧表科室名称。 */
    private String name;
    /** 科室名称。 */
    private String departmentName;
    /** 医生数量。 */
    private Integer doctorCount;
    /** 候诊描述。 */
    private String queueDesc;
    /** 父级科室编号。 */
    private Long parentId;
    /** 科室排序。 */
    private Integer sort;
    /** 科室状态。 */
    private String status;
    /** 科室说明。 */
    private String description;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 创建人编号。 */
    private Long createBy;
    /** 更新人编号。 */
    private Long updateBy;
    /** 逻辑删除标识。 */
    @TableLogic
    private Integer deleted;
}
