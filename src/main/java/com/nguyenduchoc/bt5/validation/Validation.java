package com.nguyenduchoc.bt5.validation;

import com.nguyenduchoc.bt5.jdbc.Store;

public final class Validation {
    private Validation() { }
    public static String account(String username, String fullName, String email, String phone, String password, String confirm, boolean requirePassword) {
        if (!username.matches("[A-Za-z0-9_]{3,50}")) return "Tên đăng nhập gồm 3–50 chữ cái, chữ số hoặc dấu gạch dưới.";
        if (fullName.isBlank() || fullName.length() > 100) return "Họ tên phải có từ 1 đến 100 ký tự.";
        if (email.length() > 255 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return "Email không hợp lệ.";
        if (!phone.matches("\\+?[0-9]{9,15}")) return "Số điện thoại phải có 9–15 chữ số.";
        if ((requirePassword || !password.isBlank()) && (password.length() < 6 || password.length() > 128)) return "Mật khẩu phải có 6–128 ký tự.";
        if (!password.equals(confirm)) return "Mật khẩu xác nhận không khớp.";
        return null;
    }
    public static String duplicates(Store store, String username, String email, String phone, int exceptId) {
        if (store.accountValueExists("username", username, exceptId)) return "Tên đăng nhập đã được sử dụng.";
        if (store.accountValueExists("email", email, exceptId)) return "Email đã được sử dụng.";
        if (store.accountValueExists("phone", phone, exceptId)) return "Số điện thoại đã được sử dụng.";
        return null;
    }
}
