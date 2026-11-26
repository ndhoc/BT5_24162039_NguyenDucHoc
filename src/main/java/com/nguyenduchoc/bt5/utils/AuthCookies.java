package com.nguyenduchoc.bt5.utils;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import jakarta.servlet.http.*;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.repository.IUserRepository;

/** The browser holds a random token, never a username or password. */
@org.springframework.stereotype.Component
public final class AuthCookies {
    public static final String NAME = "remember_token";
    public static final int MAX_AGE = 30 * 60;
    private final IUserRepository users;
    public AuthCookies(IUserRepository users) { this.users = users; }
    private static String token(HttpServletRequest req) {
        if (req.getCookies() != null) for (Cookie cookie : req.getCookies())
            if (NAME.equals(cookie.getName())) return cookie.getValue();
        return null;
    }
    private static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static void cookie(HttpServletRequest req, HttpServletResponse resp, String value, int age) {
        Cookie cookie = new Cookie(NAME, value);
        cookie.setMaxAge(age); cookie.setHttpOnly(true); cookie.setSecure(req.isSecure());
        cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        cookie.setAttribute("SameSite", "Lax"); resp.addCookie(cookie);
    }
    public void remember(HttpServletRequest req, HttpServletResponse resp, User user) {
        clear(req, resp);
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        users.saveRememberToken(user.getId(), hash(value), Instant.now().plusSeconds(MAX_AGE));
        cookie(req, resp, value, MAX_AGE);
    }
    public User restore(HttpServletRequest req, HttpServletResponse resp) {
        String value = token(req);
        if (value == null) return null;
        if (!value.matches("[A-Za-z0-9_-]{43}")) { cookie(req, resp, "", 0); return null; }
        User user = users.findByRememberToken(hash(value));
        if (user == null) cookie(req, resp, "", 0);
        return user;
    }
    public void clear(HttpServletRequest req, HttpServletResponse resp) {
        String value = token(req);
        if (value != null && value.matches("[A-Za-z0-9_-]{43}")) users.deleteRememberToken(hash(value));
        cookie(req, resp, "", 0);
    }
    public static void clearSessionCookie(HttpServletRequest req, HttpServletResponse resp) {
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(req.isSecure());
        cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        cookie.setAttribute("SameSite", "Lax");
        resp.addCookie(cookie);
    }
    public static void signIn(HttpServletRequest req, User user, String mode) {
        // Keep only public account data in the session.
        user.setPassword(null);
        HttpSession session = req.getSession(); req.changeSessionId();
        session.setAttribute("account", user); session.setAttribute("user", user);
        session.setAttribute("authMode", mode);
    }
}
