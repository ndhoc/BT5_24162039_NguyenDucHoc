package com.nguyenduchoc.bt5.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.service.AccountVerificationService;
import com.nguyenduchoc.bt5.service.AuthService;
import com.nguyenduchoc.bt5.service.UserService;
import com.nguyenduchoc.bt5.utils.AuthCookies;

@Controller
public class AccountController {
    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);
    private static final String PENDING = "pendingActivation";
    private final AuthService auth;
    private final UserService users;
    private final AccountVerificationService verification;
    private final AuthCookies cookies;

    public AccountController(AuthService auth, UserService users,
            AccountVerificationService verification, AuthCookies cookies) {
        this.auth = auth;
        this.users = users;
        this.verification = verification;
        this.cookies = cookies;
    }

    @GetMapping("/login") String login(HttpSession session) {
        return session.getAttribute("account") == null ? "legacy/login" : "redirect:/waiting";
    }

    @PostMapping("/login") String loginPost(HttpServletRequest req, HttpServletResponse resp) {
        String username = req.getParameter("username");
        try {
            User user = auth.authenticate(username, req.getParameter("password"));
            boolean remember = "on".equals(req.getParameter("remember"));
            if (remember) cookies.remember(req, resp, user);
            else cookies.clear(req, resp);
            AuthCookies.signIn(req, user, remember ? "Session + Cookie ghi nhớ" : "Session");
            return "redirect:/waiting";
        } catch (IllegalStateException e) {
            User pending = auth.findPending(username);
            if (pending != null) req.getSession().setAttribute(PENDING, pending.getId());
            req.setAttribute("alert", e.getMessage());
            req.setAttribute("pendingUsername", pending == null ? null : pending.getUsername());
        } catch (IllegalArgumentException e) {
            FormErrors.show(req, e);
        }
        return "legacy/login";
    }

    @GetMapping("/register") String register() { return "legacy/register"; }

    @PostMapping("/register") String registerPost(HttpServletRequest req) {
        try {
            User user = users.register(req.getParameter("username"), req.getParameter("password"),
                    req.getParameter("confirm"), req.getParameter("email"), req.getParameter("fullname"), req.getParameter("phone"));
            req.getSession().setAttribute(PENDING, user.getId());
            try {
                verification.sendActivationOtp(user);
                return "redirect:/activate?sent=1";
            } catch (IllegalStateException mailError) {
                LOG.warn("Could not send activation OTP for {}", user.getUsername(), mailError);
                req.setAttribute("alert", "Tài khoản đã được tạo nhưng chưa gửi được email OTP: "
                        + mailError.getMessage() + " Hãy dùng nút gửi lại mã bên dưới.");
                req.setAttribute("username", user.getUsername());
                return "legacy/activate";
            }
        } catch (IllegalArgumentException e) {
            FormErrors.show(req, e);
            return "legacy/register";
        }
    }

    @GetMapping("/logout") String logoutForm() { return "legacy/logout"; }

    @PostMapping("/logout") String logout(HttpServletRequest req, HttpServletResponse resp) {
        cookies.clear(req, resp);
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        AuthCookies.clearSessionCookie(req, resp);
        return "redirect:/login?loggedout=1";
    }

    @GetMapping("/activate") String activate(HttpServletRequest req) {
        HttpSession session = req.getSession();
        String username = req.getParameter("username");
        if (username != null && !username.isBlank()) {
            User user = users.get(username.trim());
            if (user != null && !user.isActive()) session.setAttribute(PENDING, user.getId());
        }
        User user = pending(session);
        if (user == null || user.isActive()) {
            session.removeAttribute(PENDING);
            req.setAttribute("lost", true);
        } else fillActivation(req, user);
        return "legacy/activate";
    }

    @PostMapping("/activate") String activatePost(HttpServletRequest req) {
        HttpSession session = req.getSession();
        User user = pending(session);
        if (user == null) {
            String username = req.getParameter("username");
            user = username == null || username.isBlank() ? null : users.get(username.trim());
            if (user != null) session.setAttribute(PENDING, user.getId());
        }
        try {
            if (user == null) throw new IllegalArgumentException("Không tìm thấy tài khoản cần kích hoạt.");
            if (user.isActive()) throw new IllegalArgumentException("Tài khoản đã được kích hoạt. Bạn có thể đăng nhập.");
            if ("resend".equals(req.getParameter("action"))) {
                verification.sendActivationOtp(user);
                req.setAttribute("message", "Mã OTP mới đã được gửi tới " + mask(user.getEmail()) + ".");
            } else {
                verification.activate(user.getId(), req.getParameter("code"));
                session.removeAttribute(PENDING);
                return "redirect:/login?activated=1";
            }
        } catch (IllegalArgumentException e) {
            FormErrors.show(req, e);
        }
        if (user != null) fillActivation(req, user);
        return "legacy/activate";
    }

    private User pending(HttpSession session) {
        Integer id = (Integer) session.getAttribute(PENDING);
        return id == null ? null : users.getById(id);
    }
    private static void fillActivation(HttpServletRequest req, User user) {
        req.setAttribute("username", user.getUsername());
        req.setAttribute("maskedEmail", mask(user.getEmail()));
    }
    static String mask(String email) {
        if (email == null || email.isBlank()) return "email đã đăng ký";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        return email.charAt(0) + "***" + email.substring(at);
    }
}
