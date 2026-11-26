package com.nguyenduchoc.bt5.controllers;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.service.ProfileService;
import com.nguyenduchoc.bt5.service.UserService;
import com.nguyenduchoc.bt5.utils.MailSender;

@Controller
public class ProfileController {
    private final UserService users;
    private final ProfileService profiles;
    private final MailSender mail;

    public ProfileController(UserService users, ProfileService profiles, MailSender mail) {
        this.users = users; this.profiles = profiles; this.mail = mail;
    }

    @GetMapping("/profile") String show(HttpServletRequest req) {
        User account = current(req.getSession());
        return account == null ? "redirect:/login" : render(req, account);
    }

    @PostMapping("/profile") String update(HttpServletRequest req,
            @RequestParam(required = false) MultipartFile avatar) throws IOException {
        User account = current(req.getSession());
        if (account == null) return "redirect:/login";
        String action = req.getParameter("action");
        try {
            if ("save-profile".equals(action)) {
                account = profiles.update(account.getId(), req.getParameter("fullname"), req.getParameter("phone"), avatar);
                refresh(req.getSession(), account);
                return "redirect:/profile?saved=profile";
            }
            if ("request-email".equals(action)) {
                profiles.requestEmailChange(account.getId(), req.getParameter("email"));
                return "redirect:/profile?saved=otp";
            }
            if ("confirm-email".equals(action)) {
                account = profiles.confirmEmailChange(account.getId(), req.getParameter("code"));
                refresh(req.getSession(), account);
                return "redirect:/profile?saved=email";
            }
            throw new IllegalArgumentException("Yêu cầu không hợp lệ.");
        } catch (IllegalArgumentException e) {
            FormErrors.show(req, e);
            req.setAttribute("profileSubmitted", "save-profile".equals(action));
            return render(req, account);
        }
    }

    private User current(HttpSession session) {
        User signedIn = (User) session.getAttribute("account");
        User account = signedIn == null ? null : users.getById(signedIn.getId());
        if (account == null || !account.isActive()) {
            session.removeAttribute("account"); session.removeAttribute("user"); return null;
        }
        refresh(session, account);
        return account;
    }
    private String render(HttpServletRequest req, User account) {
        req.setAttribute("profileAccount", account);
        req.setAttribute("pendingEmail", profiles.pendingEmail(account.getId()));
        req.setAttribute("otpViaEmail", mail.isConfigured());
        return "legacy/profile";
    }
    private static void refresh(HttpSession session, User account) {
        account.setPassword(null);
        session.setAttribute("account", account); session.setAttribute("user", account);
    }
}
