package com.hlw.gateway.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新网关路由配置请求。
 */
@Getter
@Setter
public class UpdateRouteConfigReq {
    /** 路由编码。 */
    @NotBlank(message = "路由编码不能为空")
    private String routeCode;
    /** 服务地址。 */
    @NotBlank(message = "服务地址不能为空")
    private String uri;
    /** 路径断言。 */
    @NotBlank(message = "路径断言不能为空")
    private String pathPredicate;
    /** 显示排序。 */
    private Integer sort;
    /** 路由状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}
