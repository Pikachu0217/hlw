package com.hlw.common.core.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;

/**
 * 分页查询通用参数。
 * <p>
 * 各列表接口可直接继承或组合本对象，统一分页号、每页大小与关键字检索语义。
 * </p>
 */
@Getter
@Setter
public class PageQuery {
    /** 当前页码，从 1 开始。 */
    private Integer pageNum = 1;
    /** 每页大小，默认 10。 */
    private Integer pageSize = 10;
    /** 可选的模糊搜索关键字。 */
    private String keyword;

    /**
     * 默认构造方法。
     */
    public PageQuery() {
    }

    /**
     * 转换为 MyBatis Plus 分页对象，缺省值会被回填。
     *
     * @param <T> 分页记录类型
     * @return 已初始化的分页对象
     */
    public <T> Page<T> toPage() {
        return new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
    }
}
