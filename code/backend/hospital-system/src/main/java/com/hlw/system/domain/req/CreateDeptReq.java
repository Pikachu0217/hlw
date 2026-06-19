package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建系统部门请求。
 */
@Getter
@Setter
public class CreateDeptReq {
    /** 父部门编号，根部门为 0。 */
    private Long parentId;
    /** 部门名称。 */
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    /** 显示顺序。 */
    private Integer orderNum;
    /** 负责人用户ID。 */
    private String leader;
    /** 联系电话。 */
    private String phone;
    /** 邮箱。 */
    private String email;
    /** 部门状态。 */
    private Integer status;
}
