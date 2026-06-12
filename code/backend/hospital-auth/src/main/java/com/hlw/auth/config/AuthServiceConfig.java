package com.hlw.auth.config;

import com.hlw.auth.service.AuthService;
import com.hlw.auth.service.LoginUser;
import com.hlw.auth.service.TokenIssuer;
import com.hlw.auth.service.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证模块本地启动默认配置。
 */
@Configuration
public class AuthServiceConfig {
    /**
     * 创建认证服务。
     *
     * @param userRepository 用户仓储
     * @param tokenIssuer 令牌签发器
     * @return 认证服务
     */
    @Bean
    public AuthService authService(UserRepository userRepository, TokenIssuer tokenIssuer) {
        return new AuthService(userRepository, tokenIssuer);
    }

    /**
     * 创建本地演示用户仓储。
     *
     * @return 用户仓储
     */
    @Bean
    public UserRepository userRepository() {
        return new DemoUserRepository();
    }

    /**
     * 创建本地演示令牌签发器。
     *
     * @return 令牌签发器
     */
    @Bean
    public TokenIssuer tokenIssuer() {
        return user -> "satoken-demo-" + user.id() + "-" + user.tenantId();
    }

    /**
     * 内存演示用户仓储，用于本地前后端联调。
     */
    private static final class DemoUserRepository implements UserRepository {
        private final Map<String, LoginUser> users = new ConcurrentHashMap<>();

        /**
         * 初始化默认演示账号。
         */
        private DemoUserRepository() {
            save(new LoginUser(1L, 100L, "admin", "{noop}admin123", "ADMIN"));
            save(new LoginUser(2L, 100L, "patient", "{noop}patient123", "PATIENT"));
            save(new LoginUser(3L, 100L, "运营主任", "{noop}123456", "ADMIN"));
        }

        /**
         * 保存演示用户。
         *
         * @param user 登录用户
         */
        private void save(LoginUser user) {
            users.put(user.username(), user);
        }

        /**
         * 根据账号查询演示用户。
         *
         * @param username 用户名
         * @return 登录用户
         */
        @Override
        public LoginUser findByUsername(String username) {
            return users.get(username);
        }
    }
}
