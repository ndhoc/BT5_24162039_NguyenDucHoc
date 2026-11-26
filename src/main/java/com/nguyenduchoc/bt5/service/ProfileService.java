package com.nguyenduchoc.bt5.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.springframework.web.multipart.MultipartFile;
import com.nguyenduchoc.bt5.entity.ProfileEmailChange;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.repository.DataAccessException;
import com.nguyenduchoc.bt5.repository.IProfileRepository;
import com.nguyenduchoc.bt5.repository.IProfileRepository.EmailConfirmation;
import com.nguyenduchoc.bt5.repository.IProfileRepository.SavedProfile;
import com.nguyenduchoc.bt5.utils.CloudinaryAvatarStorage;
import com.nguyenduchoc.bt5.utils.CloudinaryAvatarStorage.Avatar;
import com.nguyenduchoc.bt5.utils.MailSender;
import com.nguyenduchoc.bt5.utils.OtpCodes;
import com.nguyenduchoc.bt5.validation.FormValidation;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@org.springframework.stereotype.Service
public class ProfileService {
    private static final Duration OTP_LIFETIME = Duration.ofMinutes(10);
    private final IProfileRepository repository;
    private final CloudinaryAvatarStorage avatars;
    private final MailSender mail;

    public ProfileService(IProfileRepository repository, CloudinaryAvatarStorage avatars, MailSender mail) {
        this.repository = repository; this.avatars = avatars; this.mail = mail;
    }

    public User update(int userId, String fullName, String phone, MultipartFile image) throws IOException {
        String validName = FormValidation.requiredText("fullname", fullName, 100, "Họ tên");
        String validPhone = FormValidation.phone(phone);
        if (repository.phoneInUse(userId, validPhone))
            throw new FieldError("phone", "Số điện thoại đã được sử dụng.");
        Avatar upload = avatars.upload(image);
        SavedProfile saved = null;
        try {
            saved = repository.update(userId, validName, validPhone, upload == null ? null : upload.url(),
                    upload == null ? null : upload.publicId());
        } catch (DataAccessException e) {
            if (e.isDuplicateKey()) throw new FieldError("phone", "Số điện thoại đã được sử dụng.");
            throw e;
        } finally {
            if (saved == null && upload != null) avatars.discard(upload.publicId());
        }
        avatars.discard(saved.replacedAvatarId());
        return saved.account();
    }

    public ProfileEmailChange pendingEmail(int userId) {
        return repository.pendingEmail(userId);
    }

    public void requestEmailChange(int userId, String candidate) {
        String email = FormValidation.email(candidate);
        String code = OtpCodes.generate();
        String hash = OtpCodes.hash(code, ProfileEmailChange.hashPurpose(email), userId);
        String fullName = repository.requestEmail(userId, email, hash, Instant.now().plus(OTP_LIFETIME));
        try {
            mail.sendOtp(email, fullName, code, ProfileEmailChange.PURPOSE);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Không gửi được OTP. Vui lòng yêu cầu gửi lại mã.", e);
        }
    }

    public User confirmEmailChange(int userId, String code) {
        if (code == null || !code.trim().matches("[0-9]{6}"))
            throw new FieldError("code", "Mã OTP phải gồm 6 chữ số.");
        EmailConfirmation result;
        try {
            result = repository.confirmEmail(userId, code);
        } catch (DataAccessException e) {
            if (e.isDuplicateKey()) throw new IllegalArgumentException("Email đã được sử dụng. Vui lòng nhập địa chỉ khác.");
            throw e;
        }
        if (result.error() != null) {
            if (AccountVerificationService.OTP_INVALID.equals(result.error()))
                throw new FieldError("code", result.error());
            throw new IllegalArgumentException(result.error());
        }
        return result.account();
    }
}
