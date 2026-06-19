package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端路由展示对象。
 */
@Getter
@Setter
public class RouterResp {
    /** 路由名称。 */
    private String name;
    /** 路由地址。 */
    private String path;
    /** 组件路径。 */
    private String component;
    /** 是否隐藏。 */
    private Boolean hidden;
    /** 菜单元信息。 */
    private Meta meta;
    /** 子路由列表。 */
    private List<RouterResp> children = new ArrayList<>();

    /**
     * 路由元信息。
     */
    @Getter
    @Setter
    public static class Meta {
        /** 标题。 */
        private String title;
        /** 图标。 */
        private String icon;
    }
}
