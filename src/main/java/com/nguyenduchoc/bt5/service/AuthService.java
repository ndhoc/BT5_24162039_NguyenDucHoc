package com.nguyenduchoc.bt5.service;

import com.nguyenduchoc.bt5.entity.User;

@org.springframework.stereotype.Service
public class AuthService {

    private final UserService users;

    public AuthService(UserService users) { this.users = users; }

    /**
     * @throws IllegalArgumentException khi thông tin đăng nhập sai
     * @throws IllegalStateException khi tài khoản chưa kích hoạt bằng OTP
     */
    public User authenticate(String username, String password) {
        return users.login(username, password);
    }

    /** Tài khoản tồn tại nhưng chưa kích hoạt, dùng để mở lại trang nhập OTP. */
    public User findPending(String username) {
        if (username == null || username.isBlank()) return null;
        User user = users.get(username.trim());
        return user != null && !user.isActive() ? user : null;
    }
}
