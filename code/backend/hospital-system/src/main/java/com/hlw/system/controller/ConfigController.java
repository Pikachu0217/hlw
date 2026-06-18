package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreateConfigReq;
import com.hlw.system.domain.req.UpdateConfigReq;
import com.hlw.system.service.ConfigService;
import com.hlw.system.domain.resp.ConfigResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@Slf4j
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
    public R<PageResult<ConfigResp>> list(PageQuery query) {
        log.info("查询参数配置列表，keyword={}", query.getKeyword());
        return R.ok(configService.listConfigs(query));
    }

    /**
     * 创建系统参数配置。
     *
     * @param request 配置创建命令
     * @return 创建后的配置
     */
    @PostMapping
    public R<ConfigResp> createConfig(@Valid @RequestBody CreateConfigReq request) {
        log.info("创建系统参数配置，configKey={}", request.getConfigKey());
        return R.ok(configService.createConfig(request));
    }

    /**
     * 查询系统参数配置详情。
     *
     * @param id 配置编号
     * @return 配置详情
     */
    @GetMapping("/{id}")
    public R<ConfigResp> detail(@PathVariable Long id) {
        log.info("查询系统参数配置详情，id={}", id);
        return R.ok(configService.getConfig(id));
    }

    /**
     * 更新系统参数配置。
     *
     * @param id 配置编号
     * @param request 配置更新命令
     * @return 更新后的配置
     */
    @PutMapping("/{id}")
    public R<ConfigResp> updateConfig(@PathVariable Long id, @Valid @RequestBody UpdateConfigReq request) {
        log.info("更新系统参数配置，id={}", id);
        return R.ok(configService.updateConfig(id, request));
    }

    /**
     * 删除系统参数配置。
     *
     * @param id 配置编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteConfig(@PathVariable Long id) {
        log.info("删除系统参数配置，id={}", id);
        configService.deleteConfig(id);
        return R.ok(null);
    }
}
