package com.nguyenduchoc.bt5.controllers;

import java.io.Serializable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.service.AccountVerificationService;
import com.nguyenduchoc.bt5.service.UserService;
import com.nguyenduchoc.bt5.validation.FormValidation;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@Controller
public class PasswordRecoveryController {
    private static final String PENDING = "pendingReset";
    private static final String VERIFIED = "resetVerified";
    private record VerifiedReset(int userId, long tokenId) implements Serializable { }
    private final UserService users;
    private final AccountVerificationService verification;

    public PasswordRecoveryController(UserService users, AccountVerificationService verification) { this.users = users; this.verification = verification; }

    @GetMapping("/forgot-password") String forgot() { return "legacy/forgot-password"; }

    @PostMapping("/forgot-password") String forgotPost(HttpServletRequest req) {
        String email = req.getParameter("email");
        try {
            User user = users.getByEmail(FormValidation.email(email));
            if (user == null) throw new FieldError("email", "Email không tồn tại trong hệ thống.");
            verification.sendPasswordResetOtp(user);
            HttpSession session = req.getSession();
            session.removeAttribute(VERIFIED);
            session.setAttribute(PENDING, user.getId());
            return "redirect:/reset-password?sent=1";
        } catch (IllegalArgumentException e) {
            FormErrors.show(req, e);
            req.setAttribute("email", email);
            return "legacy/forgot-password";
        }
    }

    @GetMapping("/reset-password") String reset(HttpServletRequest req) {
        HttpSession session = req.getSession();
        User user = pendingUser(session);
        if (user == null) return expired(session);
        return render(req, session, user);
    }

    @PostMapping("/reset-password") String resetPost(HttpServletRequest req) {
        HttpSession session = req.getSession();
        User user = pendingUser(session);
        if (user == null) return expired(session);
        try {
            String action = req.getParameter("action");
            if ("resend".equals(action)) {
                verification.sendPasswordResetOtp(user);
                session.removeAttribute(VERIFIED);
                req.setAttribute("message", "Mã OTP mới đã được gửi tới " + AccountController.mask(user.getEmail()) + ".");
            } else if ("verify-code".equals(action)) {
                if (verifiedReset(session, user) != null) {
                    req.setAttribute("alert", "Mã OTP đã được xác nhận. Vui lòng nhập mật khẩu mới.");
                } else {
                    long tokenId = verification.verifyPasswordResetCode(user.getId(), req.getParameter("code"));
                    session.setAttribute(VERIFIED, new VerifiedReset(user.getId(), tokenId));
                    req.setAttribute("message", "Xác nhận mã OTP thành công. Hãy đặt mật khẩu mới.");
                }
            } else if ("set-password".equals(action)) {
                VerifiedReset verified = verifiedReset(session, user);
                if (verified == null) throw new IllegalArgumentException("Vui lòng xác nhận mã OTP trước khi đặt mật khẩu mới.");
                verification.resetPassword(user.getId(), verified.tokenId(), req.getParameter("password"), req.getParameter("confirm"));
                session.removeAttribute(PENDING); session.removeAttribute(VERIFIED);
                return "redirect:/login?reset=1";
            } else throw new IllegalArgumentException("Yêu cầu không hợp lệ.");
        } catch (IllegalArgumentException e) {
            FormErrors.show(req, e);
            if (e.getMessage().startsWith(AccountVerificationService.OTP_EXPIRED) || e.getMessage().contains("quá số lần"))
                session.removeAttribute(VERIFIED);
        }
        return render(req, session, user);
    }

    private String render(HttpServletRequest req, HttpSession session, User user) {
        req.setAttribute("step", verifiedReset(session, user) != null ? "password" : "code");
        req.setAttribute("username", user.getUsername());
        req.setAttribute("maskedEmail", AccountController.mask(user.getEmail()));
        return "legacy/reset-password";
    }
    private String expired(HttpSession session) {
        session.removeAttribute(PENDING); session.removeAttribute(VERIFIED);
        return "redirect:/forgot-password?expired=1";
    }
    private User pendingUser(HttpSession session) {
        Object userId = session.getAttribute(PENDING);
        return userId instanceof Integer id ? users.getById(id) : null;
    }
    private VerifiedReset verifiedReset(HttpSession session, User user) {
        Object value = session.getAttribute(VERIFIED);
        return value instanceof VerifiedReset verified && verified.userId() == user.getId() ? verified : null;
    }
}
