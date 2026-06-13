package com.hlw.auth.config;

import com.hlw.auth.service.AuthService;
import com.hlw.auth.service.LoginUser;
import com.hlw.auth.service.TokenIssuer;
import com.hlw.auth.service.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

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
     * @param jdbcOperations JDBC 操作组件
     * @return 用户仓储
     */
    @Bean
    public UserRepository userRepository(JdbcOperations jdbcOperations) {
        return new JdbcUserRepository(jdbcOperations);
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
     * 数据库用户仓储，用于从示例用户表读取登录账号。
     */
    private static final class JdbcUserRepository implements UserRepository {
        private final JdbcOperations jdbcOperations;

        /**
         * 构造数据库用户仓储。
         *
         * @param jdbcOperations JDBC 操作组件
         */
        private JdbcUserRepository(JdbcOperations jdbcOperations) {
            this.jdbcOperations = jdbcOperations;
        }

        /**
         * 根据账号查询数据库用户。
         *
         * @param username 用户名
         * @return 登录用户
         */
        @Override
        public LoginUser findByUsername(String username) {
            return jdbcOperations.query("""
                    SELECT id, tenant_id, username, password, user_type
                    FROM sys_user
                    WHERE username = ? AND deleted = 0 AND status IN ('启用', 'ACTIVE')
                    ORDER BY id
                    LIMIT 1
                    """,
                resultSet -> {
                    if (!resultSet.next()) {
                        return null;
                    }
                    return new LoginUser(
                        resultSet.getLong("id"),
                        resultSet.getLong("tenant_id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("user_type")
                    );
                },
                username
            );
        }
    }
}
