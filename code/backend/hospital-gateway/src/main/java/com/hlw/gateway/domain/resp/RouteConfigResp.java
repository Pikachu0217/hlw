package com.hlw.gateway.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 网关路由配置展示对象。
 */
@Getter
@Setter
public class RouteConfigResp {
    /** 表格主键。 */
    private String key;
    /** 路由编码。 */
    private String routeCode;
    /** 服务地址。 */
    private String uri;
    /** 路径断言。 */
    private String pathPredicate;
    /** 显示排序。 */
    private Integer sort;
    /** 路由状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}
