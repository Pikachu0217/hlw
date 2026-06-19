package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建菜单请求。
 */
@Getter
@Setter
public class CreateMenuReq {
    /** 菜单名称。 */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    /** 父级菜单编号。 */
    private Long parentId;
    /** 显示顺序。 */
    private Integer orderNum;
    /** 路由地址。 */
    private String path;
    /** 组件路径。 */
    private String component;
    /** 是否外链。 */
    private Integer isFrame;
    /** 菜单类型。 */
    private String menuType;
    /** 显示状态。 */
    private String visible;
    /** 状态。 */
    private String status;
    /** 权限标识。 */
    private String perms;
    /** 菜单图标。 */
    private String icon;
    /** 备注。 */
    private String remark;
}
