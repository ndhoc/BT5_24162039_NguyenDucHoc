package com.nguyenduchoc.bt5.service.impl;

import java.time.LocalDate;

import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.repository.DataAccessException;
import com.nguyenduchoc.bt5.repository.IUserRepository;
import com.nguyenduchoc.bt5.service.UserService;
import com.nguyenduchoc.bt5.utils.PasswordUtils;
import com.nguyenduchoc.bt5.validation.FormValidation;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@org.springframework.stereotype.Service
public class UserServiceImpl implements UserService {
    private final IUserRepository repository;

    public UserServiceImpl(IUserRepository repository) { this.repository = repository; }

    @Override public User get(String username) { return repository.get(username); }
    @Override public User getById(int id) { return repository.getById(id); }
    @Override public User getByEmail(String email) {
        return email == null || email.isBlank() ? null : repository.getByEmail(email.trim());
    }
    @Override public java.util.List<User> all() { return repository.all(); }

    @Override public User login(String username, String password) {
        FormValidation.login(username, password);
        User user = repository.get(username.trim());
        if (user == null || !PasswordUtils.verify(password, user.getPassword()))
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng.");
        if (!user.isActive())
            throw new IllegalStateException("Tài khoản chưa được kích hoạt. Vui lòng nhập mã OTP gửi tới email của bạn.");
        return user;
    }

    @Override public boolean checkExistUsername(String v) { return repository.checkExistUsername(v); }
    @Override public boolean checkExistEmail(String v) { return repository.checkExistEmail(v); }
    @Override public boolean checkExistPhone(String v) { return repository.checkExistPhone(v); }
    @Override public long count() { return repository.count(); }

    @Override public User register(String username, String password, String confirm, String email, String fullname, String phone) {
        username = FormValidation.username(username);
        email = FormValidation.email(email);
        fullname = FormValidation.requiredText("fullname", fullname, 100, "Họ tên");
        phone = FormValidation.phone(phone);
        FormValidation.newPassword(password, confirm);
        if (checkExistUsername(username)) throw new FieldError("username", "Tài khoản đã tồn tại.");
        if (checkExistEmail(email)) throw new FieldError("email", "Email đã tồn tại.");
        if (checkExistPhone(phone)) throw new FieldError("phone", "Số điện thoại đã tồn tại.");
        User user = new User(username, fullname);
        user.setEmail(email); user.setPhone(phone); user.setPassword(PasswordUtils.hash(password));
        user.setRoleid(5); user.setCreatedDate(LocalDate.now()); user.setActive(Boolean.FALSE);
        try { repository.insert(user); }
        catch (DataAccessException e) {
            if (e.isDuplicateKey()) throw new IllegalArgumentException("Tài khoản, email hoặc số điện thoại đã tồn tại.");
            throw e;
        }
        return user;
    }

}
