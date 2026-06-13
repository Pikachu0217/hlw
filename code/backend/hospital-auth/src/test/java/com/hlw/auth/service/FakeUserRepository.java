package com.hlw.auth.service;

import com.hlw.auth.vo.UserProfileVO;

import java.util.HashMap;
import java.util.Map;

class FakeUserRepository implements UserRepository {
    private final Map<String, LoginUser> users = new HashMap<>();

    void save(LoginUser user) {
        users.put(user.username(), user);
    }

    @Override
    public LoginUser findByUsername(String username) {
        return users.get(username);
    }

    @Override
    public UserProfileVO findProfileById(Long id, Long tenantId) {
        return users.values().stream()
            .filter(user -> user.id().equals(id) && user.tenantId().equals(tenantId))
            .findFirst()
            .map(this::toProfile)
            .orElse(null);
    }

    /**
     * 转换登录用户资料。
     *
     * @param user 登录用户
     * @return 用户资料
     */
    private UserProfileVO toProfile(LoginUser user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setKey(String.valueOf(user.id()));
        vo.setUserId(user.id());
        vo.setTenantId(user.tenantId());
        vo.setUsername(user.username());
        vo.setUserType(user.userType());
        vo.setRoleName(user.userType());
        vo.setStatus("启用");
        return vo;
    }
}
