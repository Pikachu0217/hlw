package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 租户套餐展示对象。
 */
@Getter
@Setter
public class TenantPackageResp {
    /** 套餐编号。 */
    private Long id;
    /** 套餐名称。 */
    private String packageName;
    /** 备注。 */
    private String remark;
    /** 状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
    /** 菜单编号列表。 */
    private List<Long> menuIds;
}
