package com.nguyenduchoc.bt5;

import com.nguyenduchoc.bt5.validation.Validation;
import com.nguyenduchoc.bt5.utils.PasswordUtils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AuthValidationTest {
    @Test void passwordHashVerifiesOnlyOriginalPassword() {
        String hash = PasswordUtils.hash("secure-pass-123");
        assertTrue(PasswordUtils.verify("secure-pass-123", hash));
        assertFalse(PasswordUtils.verify("wrong", hash));
        assertNotEquals(hash, PasswordUtils.hash("secure-pass-123"));
    }
    @Test void registrationValidationRejectsBadFields() {
        assertNull(Validation.account("member_1", "Nguyễn Đức Học", "hoc@example.com", "0912345678", "secret123", "secret123", true));
        assertNotNull(Validation.account("a", "Nguyễn Đức Học", "hoc@example.com", "0912345678", "secret123", "secret123", true));
        assertNotNull(Validation.account("member_1", "Nguyễn Đức Học", "bad-email", "0912345678", "secret123", "secret123", true));
        assertNotNull(Validation.account("member_1", "Nguyễn Đức Học", "hoc@example.com", "0912345678", "secret123", "different", true));
    }
}
