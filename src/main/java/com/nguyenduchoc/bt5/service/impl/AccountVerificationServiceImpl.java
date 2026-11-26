package com.nguyenduchoc.bt5.service.impl;

import java.time.Duration;
import java.time.Instant;

import com.nguyenduchoc.bt5.entity.OtpToken;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.repository.IOtpRepository;
import com.nguyenduchoc.bt5.repository.IUserRepository;
import com.nguyenduchoc.bt5.service.AccountVerificationService;
import com.nguyenduchoc.bt5.utils.MailSender;
import com.nguyenduchoc.bt5.utils.OtpCodes;
import com.nguyenduchoc.bt5.utils.PasswordUtils;
import com.nguyenduchoc.bt5.validation.FormValidation;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@org.springframework.stereotype.Service
public class AccountVerificationServiceImpl implements AccountVerificationService {
    private static final Duration LIFETIME = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS = 5;

    private final IOtpRepository tokens;
    private final IUserRepository users;
    private final MailSender mail;

    public AccountVerificationServiceImpl(IOtpRepository tokens, IUserRepository users, MailSender mail) {
        this.tokens = tokens; this.users = users; this.mail = mail;
    }

    @Override
    public void sendActivationOtp(User user) {
        issue(user, OtpToken.PURPOSE_ACTIVATION);
    }

    @Override
    public void activate(int userId, String code) {
        verify(userId, OtpToken.PURPOSE_ACTIVATION, code);
        User user = users.getById(userId);
        if (user == null) throw new IllegalArgumentException("Tài khoản không tồn tại.");
        user.setActive(Boolean.TRUE);
        users.update(user);
        tokens.delete(userId, OtpToken.PURPOSE_ACTIVATION);
    }

    @Override
    public void sendPasswordResetOtp(User user) {
        issue(user, OtpToken.PURPOSE_RESET);
    }

    @Override
    public long verifyPasswordResetCode(int userId, String code) {
        return verify(userId, OtpToken.PURPOSE_RESET, code).getId();
    }

    @Override
    public void resetPassword(int userId, long tokenId, String password, String confirm) {
        FormValidation.newPassword(password, confirm);
        if (!tokens.completePasswordReset(userId, tokenId, PasswordUtils.hash(password)))
            throw new IllegalArgumentException(OTP_EXPIRED);
    }

    private void issue(User user, String purpose) {
        String code = OtpCodes.generate();
        OtpToken token = new OtpToken(user, purpose, OtpCodes.hash(code, purpose, user.getId()),
                Instant.now().plus(LIFETIME));
        tokens.replace(token);
        mail.sendOtp(user.getEmail(), user.getFullName(), code, purpose);
    }

    private OtpToken verify(int userId, String purpose, String code) {
        if (code == null || !code.trim().matches("[0-9]{6}"))
            throw new FieldError("code", "Mã OTP phải gồm 6 chữ số.");
        OtpToken token = tokens.find(userId, purpose);
        if (token == null || token.getExpiresAt().isBefore(Instant.now())) {
            if (token != null) tokens.delete(userId, purpose);
            throw new IllegalArgumentException(OTP_EXPIRED);
        }
        if (token.getAttempts() >= MAX_ATTEMPTS) {
            tokens.delete(userId, purpose);
            throw new IllegalArgumentException("Bạn đã nhập sai quá số lần cho phép. Vui lòng yêu cầu mã mới.");
        }
        if (!OtpCodes.matches(code, token.getCodeHash(), purpose, userId)) {
            tokens.incrementAttempts(token.getId());
            throw new FieldError("code", OTP_INVALID);
        }
        return token;
    }
}
