package com.hlw.gateway.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 网关路由配置持久化对象。
 */
@Getter
@Setter
@TableName("gw_route_config")
public class GwRouteConfigEntity extends BaseEntity {
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
