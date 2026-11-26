package com.nguyenduchoc.bt5.config;

import java.security.SecureRandom;
import java.util.Base64;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.service.UserService;
import com.nguyenduchoc.bt5.utils.AuthCookies;

@Component
public class WebAccess implements WebMvcConfigurer, HandlerInterceptor {
    private final UserService users;
    private final AuthCookies cookies;
    private final SecureRandom random = new SecureRandom();

    public WebAccess(UserService users, AuthCookies cookies) { this.users = users; this.cookies = cookies; }

    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/templates/**", "/videos/**");
    }

    @Override public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Cache-Control", "no-store");
        HttpSession session = req.getSession();
        if (session.getAttribute("csrf") == null) {
            byte[] token = new byte[32]; random.nextBytes(token);
            session.setAttribute("csrf", Base64.getUrlEncoder().withoutPadding().encodeToString(token));
        }
        String path = req.getRequestURI().substring(req.getContextPath().length());
        User account = (User) session.getAttribute("account");
        if (account == null && !path.equals("/logout")) {
            account = cookies.restore(req, resp);
            if (account != null) AuthCookies.signIn(req, account, "Cookie");
        }
        if (account != null) {
            account = users.getById(account.getId());
            if (account == null || !account.isActive()) {
                session.removeAttribute("account"); session.removeAttribute("user"); account = null;
            } else {
                account.setPassword(null);
                session.setAttribute("account", account); session.setAttribute("user", account);
            }
        }
        if (path.startsWith("/admin/")) {
            if (account == null) { resp.sendRedirect(req.getContextPath() + "/login"); return false; }
            if (account.getRoleid() != 1) { resp.sendError(HttpStatus.FORBIDDEN.value()); return false; }
        }
        if (path.equals("/dashboard") && (account == null || account.getRoleid() != 1 && account.getRoleid() != 2)) {
            if (account == null) resp.sendRedirect(req.getContextPath() + "/login");
            else resp.sendError(HttpStatus.FORBIDDEN.value());
            return false;
        }
        if ((path.equals("/profile") || path.equals("/video/upload")) && account == null) {
            resp.sendRedirect(req.getContextPath() + "/login"); return false;
        }
        if ("POST".equals(req.getMethod()) && !session.getAttribute("csrf").equals(req.getParameter("csrf"))) {
            resp.sendError(HttpStatus.FORBIDDEN.value(), "Phiên làm việc không hợp lệ. Hãy tải lại biểu mẫu.");
            return false;
        }
        return true;
    }
}
