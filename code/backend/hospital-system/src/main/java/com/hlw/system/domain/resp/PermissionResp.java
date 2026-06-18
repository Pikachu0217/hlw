package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 权限码展示对象。
 */
@Getter
@Setter
public class PermissionResp {
    /** 表格主键。 */
    private String key;
    /** 权限名称。 */
    private String permissionName;
    /** 权限编码。 */
    private String permissionCode;
    /** 资源类型。 */
    private String resourceType;
    /** 菜单名称。 */
    private String menuName;
    /** 状态。 */
    private String status;
}
