package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.UpdateConfigRequest;
import com.hlw.system.service.ConfigService;
import com.hlw.system.vo.ConfigVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 参数配置管理控制器。
 */
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class ConfigController {
    /** 参数配置聚合服务。 */
    private final ConfigService configService;

    /**
     * 分页查询系统参数配置列表。
     *
     * @param query 分页查询参数
     * @return 系统参数配置分页结果
     */
    @GetMapping
    public R<PageResult<ConfigVO>> list(PageQuery query) {
        return R.ok(configService.listConfigs(query));
    }

    /**
     * 更新系统参数配置。
     *
     * @param id 配置编号
     * @param request 配置更新命令
     * @return 更新后的配置
     */
    @PutMapping("/{id}")
    public R<ConfigVO> updateConfig(@PathVariable Long id, @Valid @RequestBody UpdateConfigRequest request) {
        return R.ok(configService.updateConfig(id, request));
    }
}
