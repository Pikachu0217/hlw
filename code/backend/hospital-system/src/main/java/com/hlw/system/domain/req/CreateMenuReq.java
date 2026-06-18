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
    /** 权限标识。 */
    @NotBlank(message = "权限标识不能为空")
    private String permission;
    /** 路由路径。 */
    @NotBlank(message = "路由路径不能为空")
    private String routePath;
    /** 菜单类型。 */
    private String menuType;
    /** 父级菜单编号。 */
    private Long parentId;
    /** 排序。 */
    private Integer sort;
    /** 状态。 */
    private String status;
}
