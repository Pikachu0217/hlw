package com.hlw.system.web;

import com.hlw.common.core.security.TokenPrincipal;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.system.entity.SysOperatorLogEntity;
import com.hlw.system.service.OperatorLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统模块操作日志拦截器，负责采集后台接口请求并写入 sys_operator_log。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemOperatorLogInterceptor implements HandlerInterceptor {
    /** 请求开始时间属性名。 */
    private static final String START_TIME_ATTRIBUTE = SystemOperatorLogInterceptor.class.getName() + ".START_TIME";
    /** 正常状态。 */
    private static final int SUCCESS_STATUS = 0;
    /** 异常状态。 */
    private static final int FAILURE_STATUS = 1;
    /** 其它业务类型。 */
    private static final int OTHER_BUSINESS_TYPE = 0;
    /** 新增业务类型。 */
    private static final int INSERT_BUSINESS_TYPE = 1;
    /** 修改业务类型。 */
    private static final int UPDATE_BUSINESS_TYPE = 2;
    /** 删除业务类型。 */
    private static final int DELETE_BUSINESS_TYPE = 3;
    /** 后台用户操作类别。 */
    private static final int ADMIN_OPERATOR_TYPE = 1;
    /** 手机端用户操作类别。 */
    private static final int MOBILE_OPERATOR_TYPE = 2;
    /** 系统操作日志服务。 */
    private final OperatorLogService operatorLogService;

    /**
     * 请求进入控制器前记录开始时间。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器
     * @return 是否继续执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    /**
     * 请求完成后写入操作日志。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器
     * @param ex 处理异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }
        try {
            SysOperatorLogEntity entity = buildOperatorLog(request, response, handlerMethod, ex);
            operatorLogService.recordOperatorLog(entity);
        } catch (RuntimeException exception) {
            log.warn("写入系统操作日志失败，uri={}，message={}", request.getRequestURI(), exception.getMessage());
        }
    }

    /**
     * 构造操作日志持久化对象。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handlerMethod 处理方法
     * @param ex 处理异常
     * @return 操作日志持久化对象
     */
    private SysOperatorLogEntity buildOperatorLog(
            HttpServletRequest request,
            HttpServletResponse response,
            HandlerMethod handlerMethod,
            Exception ex
    ) {
        TokenPrincipal principal = TokenPrincipalContext.get();
        SysOperatorLogEntity entity = new SysOperatorLogEntity();
        entity.setTenantId(resolveTenantId(principal));
        entity.setTitle(resolveTitle(request, handlerMethod));
        entity.setBusinessType(resolveBusinessType(request.getMethod()));
        entity.setMethod(truncate(handlerMethod.getBeanType().getName() + "." + handlerMethod.getMethod().getName(), 100));
        entity.setRequestMethod(truncate(request.getMethod(), 10));
        entity.setOperatorType(resolveOperatorType(principal));
        entity.setOperatorName(truncate(resolveOperatorName(principal), 50));
        entity.setDeptName("");
        entity.setOperatorUrl(truncate(request.getRequestURI(), 255));
        entity.setOperatorIp(truncate(resolveClientIp(request), 128));
        entity.setOperatorParam(truncate(resolveRequestParam(request), 2000));
        entity.setJsonResult("");
        entity.setStatus(resolveStatus(response, ex));
        entity.setErrorMsg(truncate(ex == null ? "" : ex.getMessage(), 2000));
        entity.setOperatorTime(LocalDateTime.now());
        entity.setCostTime(resolveCostTime(request));
        return entity;
    }

    /**
     * 解析操作状态。
     *
     * @param response HTTP 响应
     * @param ex 处理异常
     * @return 操作状态
     */
    private Integer resolveStatus(HttpServletResponse response, Exception ex) {
        int status = response.getStatus();
        return ex == null && status >= 200 && status < 400 ? SUCCESS_STATUS : FAILURE_STATUS;
    }

    /**
     * 解析租户编号。
     *
     * @param principal 当前登录主体
     * @return 租户编号
     */
    private String resolveTenantId(TokenPrincipal principal) {
        Long tenantId = principal == null ? null : principal.getTenantId();
        return tenantId == null ? "-1" : String.valueOf(tenantId);
    }

    /**
     * 解析模块标题。
     *
     * @param request HTTP 请求
     * @param handlerMethod 处理方法
     * @return 模块标题
     */
    private String resolveTitle(HttpServletRequest request, HandlerMethod handlerMethod) {
        String uri = request.getRequestURI();
        String[] parts = uri.split("/");
        if (parts.length >= 3 && !parts[2].isBlank()) {
            return truncate(URLDecoder.decode(parts[2], StandardCharsets.UTF_8), 50);
        }
        return truncate(handlerMethod.getBeanType().getSimpleName(), 50);
    }

    /**
     * 解析业务类型。
     *
     * @param method HTTP 方法
     * @return 业务类型
     */
    private Integer resolveBusinessType(String method) {
        if ("POST".equalsIgnoreCase(method)) {
            return INSERT_BUSINESS_TYPE;
        }
        if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            return UPDATE_BUSINESS_TYPE;
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            return DELETE_BUSINESS_TYPE;
        }
        return OTHER_BUSINESS_TYPE;
    }

    /**
     * 解析操作类别。
     *
     * @param principal 当前登录主体
     * @return 操作类别
     */
    private Integer resolveOperatorType(TokenPrincipal principal) {
        String userType = principal == null ? "" : principal.getUserType();
        return "PATIENT".equalsIgnoreCase(userType) ? MOBILE_OPERATOR_TYPE : ADMIN_OPERATOR_TYPE;
    }

    /**
     * 解析操作人名称。
     *
     * @param principal 当前登录主体
     * @return 操作人名称
     */
    private String resolveOperatorName(TokenPrincipal principal) {
        Long userId = principal == null ? null : principal.getUserId();
        return userId == null ? "anonymous" : String.valueOf(userId);
    }

    /**
     * 解析客户端 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 解析请求参数。
     *
     * @param request HTTP 请求
     * @return 请求参数文本
     */
    private String resolveRequestParam(HttpServletRequest request) {
        if (request.getParameterMap().isEmpty()) {
            return "";
        }
        return request.getParameterMap().entrySet().stream()
            .map(this::formatParameter)
            .collect(Collectors.joining("&"));
    }

    /**
     * 格式化请求参数。
     *
     * @param entry 请求参数项
     * @return 参数文本
     */
    private String formatParameter(Map.Entry<String, String[]> entry) {
        return entry.getKey() + "=" + Arrays.toString(entry.getValue());
    }

    /**
     * 解析请求耗时。
     *
     * @param request HTTP 请求
     * @return 请求耗时毫秒数
     */
    private Long resolveCostTime(HttpServletRequest request) {
        Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime instanceof Long startTimestamp) {
            return Math.max(System.currentTimeMillis() - startTimestamp, 0L);
        }
        return 0L;
    }

    /**
     * 截断文本。
     *
     * @param value 原始文本
     * @param maxLength 最大长度
     * @return 截断后文本
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
