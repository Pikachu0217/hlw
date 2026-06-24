package com.hlw.auth.service;

import com.hlw.auth.client.CreatePatientFeignReq;
import com.hlw.auth.client.PatientFeignClient;
import com.hlw.auth.domain.dto.TokenIssuer;
import com.hlw.auth.domain.req.CreatePatientUserFeignReq;
import com.hlw.auth.domain.req.LoginReq;
import com.hlw.auth.domain.req.PhoneCodeReq;
import com.hlw.auth.domain.req.PhoneLoginReq;
import com.hlw.auth.domain.req.SwitchTenantReq;
import com.hlw.auth.domain.resp.LoginResultResp;
import com.hlw.auth.domain.resp.LoginUserResp;
import com.hlw.auth.domain.resp.UserDetailResp;
import com.hlw.auth.client.UserFeignClient;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.resp.InternalUserResp;
import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.enums.HttpStatusEnum;
import com.hlw.common.core.enums.RedisKeyEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.security.TokenPrincipal;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.common.core.util.JwtUtil;
import com.hlw.common.redis.service.RedisService;
import com.hlw.common.security.PasswordEncoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;

/**
 * 认证业务服务，负责账号登录和登录用户资料读取。
 */
@Service
@Slf4j
public class AuthService {
    private static final String PATIENT_USER_TYPE = "patient";

    private final UserRepository userRepository;
    private final UserFeignClient userFeignClient;
    private final PatientFeignClient patientFeignClient;
    private final TokenIssuer tokenIssuer;
    private final String jwtSecret;
    private final AuthTokenProperties authTokenProperties;
    private final RedisService redisService;
    private final LoginAuditService loginAuditService;

    /**
     * 构造认证服务。
     *
     * @param userRepository 用户仓储
     * @param tokenIssuer 令牌签发器
     * @param jwtSecret JWT 签名密钥
     * @param authTokenProperties 公共认证令牌配置属性
     * @param redisService Redis 操作服务，用于维护退出登录令牌黑名单
     * @param loginAuditService 登录审计服务
     */
    public AuthService(UserRepository userRepository, UserFeignClient userFeignClient,
                       PatientFeignClient patientFeignClient, TokenIssuer tokenIssuer,
                       @Value("${hlw.jwt.secret}") String jwtSecret,
                       AuthTokenProperties authTokenProperties,
                       RedisService redisService,
                       LoginAuditService loginAuditService) {
        this.userRepository = userRepository;
        this.userFeignClient = userFeignClient;
        this.patientFeignClient = patientFeignClient;
        this.tokenIssuer = tokenIssuer;
        this.jwtSecret = jwtSecret;
        this.authTokenProperties = authTokenProperties;
        this.redisService = redisService;
        this.loginAuditService = loginAuditService;
    }

    /**
     * 执行账号密码登录。
     *
     * @param loginReq 登录命令
     * @return 登录结果
     */
    public LoginResultResp login(LoginReq loginReq) {
        return login(loginReq, "", "");
    }

    /**
     * 执行账号密码登录，并记录客户端信息。
     *
     * @param loginReq 登录命令
     * @param clientIp 客户端 IP
     * @param userAgent 客户端标识
     * @return 登录结果
     */
    public LoginResultResp login(LoginReq loginReq, String clientIp, String userAgent) {
        if (loginReq == null) {
            log.warn("用户登录认证失败，登录参数为空");
            throw new BizException(HttpStatusEnum.LOGIN_PARAMETERS_CANNOT_BE_NULL);
        }
        Long tenantId = requireLoginTenantId(loginReq.tenantId());
        log.info("用户登录认证开始，tenantId={}，username={}", tenantId, loginReq.username());
        LoginUserResp user = userRepository.findByTenantIdAndUsername(tenantId, loginReq.username());
        if (user == null || !matches(loginReq.password(), user.password())) {
            log.warn("用户登录认证失败，tenantId={}，username={}", tenantId, loginReq.username());
            loginAuditService.recordLoginFailure(tenantId, loginReq.username(), "用户名或密码错误", clientIp, userAgent);
            throw new BizException(HttpStatusEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        String token = tokenIssuer.issue(user);
        loginAuditService.recordLoginSuccess(user, token, clientIp, userAgent);
        log.info("用户登录认证成功，userId={}，tenantId={}", user.id(), user.tenantId());
        return new LoginResultResp(token, String.valueOf(user.tenantId()), user.username(), user.realName(), user.userType());
    }

    /**
     * 按令牌读取登录用户资料。
     *
     * @return 登录用户资料
     */
    public UserDetailResp detail() {
        TokenPrincipal principal = TokenPrincipalContext.get();
        log.info("查询登录用户资料，userId={}，tenantId={}", principal.getUserId(), principal.getTenantId());
        UserDetailResp detail = userRepository.findProfileById(principal.getUserId(), principal.getTenantId());
        if (detail == null) {
            throw new BizException(HttpStatusEnum.LOGIN_USER_DOES_NOT_EXIST);
        }
        return detail;
    }

    /**
     * 注销登录令牌，将令牌写入 Redis 黑名单直至原令牌过期。
     * <p>令牌已过期或缺失时直接返回，不再写入 Redis；令牌格式无效时记录告警但不抛错，
     * 避免攻击者通过 logout 接口探测令牌格式。</p>
     *
     * @param rawToken 已剥离前缀的原始登录令牌
     */
    public void logout(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            log.warn("退出登录失败，令牌为空");
            throw new BizException(HttpStatusEnum.LOGOUT_TOKEN_REQUIRED);
        }
        Duration remaining = remainingValidity(rawToken);
        if (remaining == null) {
            log.info("退出登录令牌已失效，无需写入黑名单");
            return;
        }
        String key = RedisKeyEnum.getKey(RedisKeyEnum.AUTH_LOGOUT_BLACKLIST, rawToken);
        boolean success = redisService.set(key, "1", remaining);
        if (!success) {
            log.warn("退出登录写入黑名单失败，key={}", key);
        } else {
            log.info("退出登录写入黑名单成功，ttlSeconds={}", remaining.toSeconds());
        }
        loginAuditService.recordLogout(rawToken);
    }

    /**
     * 计算令牌相对当前时间的剩余有效时长。
     *
     * @param rawToken 原始登录令牌
     * @return 剩余时长；令牌已过期、无过期时间或解析失败时返回 null
     */
    private Duration remainingValidity(String rawToken) {
        try {
            Claims claims = JwtUtil.parseClaims(rawToken, jwtSecret);
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return null;
            }
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            return remainingMs > 0L ? Duration.ofMillis(remainingMs) : null;
        } catch (JwtException exception) {
            log.warn("退出登录解析令牌失败，{}", exception.getMessage());
            return null;
        }
    }

    /**
     * 发送手机验证码到患者端。
     * <p>验证码固定为 1234，以手机号为 key 存入 Redis，TTL 300 秒。</p>
     *
     * @param phoneCodeReq 手机号请求
     */
    public void sendPhoneCode(PhoneCodeReq phoneCodeReq) {
        String phone = phoneCodeReq.phone();
        Long tenantId = requireLoginTenantId(phoneCodeReq.tenantId());
        log.info("发送手机验证码，tenantId={}，phone={}", tenantId, phone);
        String code = "1234";
        String key = RedisKeyEnum.getKey(RedisKeyEnum.AUTH_PHONE_CODE, tenantId, phone);
        redisService.set(key, code, Duration.ofMillis(RedisKeyEnum.AUTH_PHONE_CODE.getTimeoutMillis()));
        log.info("手机验证码已存入 Redis，tenantId={}，phone={}，code={}", tenantId, phone, code);
    }

    /**
     * 执行手机号+验证码登录。
     *
     * @param phoneLoginReq 手机号登录请求
     * @param clientIp      客户端 IP
     * @param userAgent     客户端标识
     * @return 登录结果
     */
    public LoginResultResp phoneLogin(PhoneLoginReq phoneLoginReq, String clientIp, String userAgent) {
        if (phoneLoginReq == null) {
            log.warn("手机号登录失败，参数为空");
            throw new BizException(HttpStatusEnum.LOGIN_PARAMETERS_CANNOT_BE_NULL);
        }
        String phone = phoneLoginReq.phone();
        String smsCode = phoneLoginReq.smsCode();
        log.info("手机号登录开始，phone={}", phone);

        // 从 Redis 获取验证码
        Long tenantId = requireLoginTenantId(phoneLoginReq.tenantId());
        String key = RedisKeyEnum.getKey(RedisKeyEnum.AUTH_PHONE_CODE, tenantId, phone);
        String cachedCode = redisService.get(key);
        if (cachedCode == null) {
            log.warn("手机号登录失败，验证码已过期，phone={}", phone);
            throw new BizException(HttpStatusEnum.SMS_CODE_EXPIRED);
        }
        if (!"1234".equals(smsCode)) {
            log.warn("手机号登录失败，验证码不匹配，phone={}", phone);
            throw new BizException(HttpStatusEnum.SMS_CODE_NOT_MATCH);
        }
        // 验证码匹配后删除 key，防止重复使用
        redisService.delete(key);

        LoginUserResp user = userRepository.findByTenantIdAndPhone(tenantId, phone);
        if (user == null) {
            log.info("手机号未注册，自动注册 phone={}", phone);
            // 1. 在 system 创建用户
            String userName = "p_" + phone;
            R<InternalUserResp> createResp = userFeignClient.createPatientUser(new CreatePatientUserFeignReq(tenantId, userName, phone));
            if (createResp == null || createResp.code() != 200 || createResp.data() == null) {
                log.warn("自动注册用户失败，phone={}", phone);
                loginAuditService.recordLoginFailure(tenantId, phone, "自动注册失败", clientIp, userAgent);
                throw new BizException(HttpStatusEnum.LOGIN_PARAMETERS_CANNOT_BE_NULL);
            }
            InternalUserResp created = createResp.data();
            // 2. 在 patient 创建患者档案（使用 sys_user.user_id 字符串）
            patientFeignClient.createOrGetByUser(new CreatePatientFeignReq(tenantId, created.getUserId(), phone));
            // 3. 重新查询用户
            user = userRepository.findByTenantIdAndPhone(tenantId, phone);
            if (user == null) {
                log.warn("自动注册后查询用户仍为空，phone={}", phone);
                loginAuditService.recordLoginFailure(tenantId, phone, "自动注册后查询失败", clientIp, userAgent);
                throw new BizException(HttpStatusEnum.LOGIN_USER_DOES_NOT_EXIST);
            }
            log.info("自动注册成功，userId={}，phone={}", user.id(), phone);
        }
        ensurePatientLoginUser(user, tenantId, phone, "患者端手机号登录");
        String token = tokenIssuer.issue(user);
        loginAuditService.recordLoginSuccess(user, token, clientIp, userAgent);
        log.info("手机号登录成功，userId={}，tenantId={}，phone={}", user.id(), user.tenantId(), phone);
        return new LoginResultResp(token, String.valueOf(user.tenantId()), user.username(), user.realName(), user.userType());
    }

    /**
     * 切换当前登录用户租户并签发目标租户令牌。
     *
     * @param switchTenantReq 切换租户请求
     * @param clientIp        客户端 IP
     * @param userAgent       客户端标识
     * @return 登录结果
     */
    public LoginResultResp switchTenant(SwitchTenantReq switchTenantReq, String clientIp, String userAgent) {
        if (switchTenantReq == null) {
            log.warn("切换租户失败，请求参数为空");
            throw new BizException(HttpStatusEnum.LOGIN_PARAMETERS_CANNOT_BE_NULL);
        }
        Long targetTenantId = requireLoginTenantId(switchTenantReq.tenantId());
        TokenPrincipal principal = TokenPrincipalContext.get();
        if (principal == null || principal.getUserId() == null || principal.getTenantId() == null) {
            log.warn("切换租户失败，登录上下文为空");
            throw new BizException(401, "当前登录用户无效");
        }
        log.info("切换登录租户开始，currentTenantId={}，targetTenantId={}，userId={}",
            principal.getTenantId(), targetTenantId, principal.getUserId());

        UserDetailResp currentUser = userRepository.findProfileById(principal.getUserId(), principal.getTenantId());
        if (currentUser == null || !StringUtils.hasText(currentUser.getPhone())) {
            log.warn("切换租户失败，当前登录用户手机号为空，userId={}，tenantId={}", principal.getUserId(), principal.getTenantId());
            throw new BizException(400, "当前登录用户缺少手机号，无法切换医院");
        }
        if (!PATIENT_USER_TYPE.equalsIgnoreCase(currentUser.getUserType())) {
            log.warn("切换租户失败，当前账号不是患者账号，userId={}，userType={}", principal.getUserId(), currentUser.getUserType());
            throw new BizException(403, "仅患者账号支持切换医院");
        }

        LoginUserResp targetUser = userRepository.findByTenantIdAndPhone(targetTenantId, currentUser.getPhone());
        if (targetUser == null) {
            log.info("目标租户患者账号不存在，自动创建，targetTenantId={}，phone={}", targetTenantId, currentUser.getPhone());
            String userName = "p_" + currentUser.getPhone();
            R<InternalUserResp> createResp = userFeignClient.createPatientUser(new CreatePatientUserFeignReq(targetTenantId, userName, currentUser.getPhone()));
            if (createResp == null || createResp.code() != 200 || createResp.data() == null) {
                log.warn("切换租户自动创建用户失败，targetTenantId={}，phone={}", targetTenantId, currentUser.getPhone());
                throw new BizException(500, "切换医院失败，无法创建目标医院患者账号");
            }
            InternalUserResp created = createResp.data();
            patientFeignClient.createOrGetByUser(new CreatePatientFeignReq(targetTenantId, created.getUserId(), currentUser.getPhone()));
            targetUser = userRepository.findByTenantIdAndPhone(targetTenantId, currentUser.getPhone());
        } else {
            ensurePatientLoginUser(targetUser, targetTenantId, currentUser.getPhone(), "切换登录租户");
            patientFeignClient.createOrGetByUser(new CreatePatientFeignReq(targetTenantId, targetUser.userId(), currentUser.getPhone()));
        }

        if (targetUser == null) {
            log.warn("切换租户失败，目标租户用户仍不存在，targetTenantId={}，phone={}", targetTenantId, currentUser.getPhone());
            throw new BizException(HttpStatusEnum.LOGIN_USER_DOES_NOT_EXIST);
        }

        String token = tokenIssuer.issue(targetUser);
        loginAuditService.recordLoginSuccess(targetUser, token, clientIp, userAgent);
        log.info("切换登录租户成功，targetTenantId={}，userId={}，phone={}", targetTenantId, targetUser.id(), currentUser.getPhone());
        return new LoginResultResp(token, String.valueOf(targetUser.tenantId()), targetUser.username(), targetUser.realName(), targetUser.userType());
    }

    /**
     * 校验手机号对应账号是否为患者账号。
     *
     * @param user 登录账号
     * @param tenantId 租户编号
     * @param phone 手机号
     * @param scene 业务场景
     */
    private void ensurePatientLoginUser(LoginUserResp user, Long tenantId, String phone, String scene) {
        if (user != null && !PATIENT_USER_TYPE.equalsIgnoreCase(user.userType())) {
            log.warn("{}失败，手机号已绑定非患者账号，tenantId={}，phone={}，userType={}",
                scene, tenantId, phone, user.userType());
            throw new BizException(403, "目标医院手机号已绑定非患者账号");
        }
    }

    /**
     * 校验明文密码和存储密码是否匹配。
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 存储密码
     * @return 是否匹配
     */
    private boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        return PasswordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 校验登录租户编号。
     *
     * @param tenantId 租户编号
     * @return 有效租户编号
     */
    private Long requireLoginTenantId(Long tenantId) {
        if (tenantId == null || tenantId < CommonConstants.PLATFORM_TENANT_ID) {
            log.warn("用户登录认证失败，租户编号无效，tenantId={}", tenantId);
            throw new BizException(HttpStatusEnum.LOGIN_PARAMETERS_CANNOT_BE_NULL);
        }
        return tenantId;
    }
}
