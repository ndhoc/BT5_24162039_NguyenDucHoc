package com.nguyenduchoc.bt5.jdbc;

import com.nguyenduchoc.bt5.utils.PasswordUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.nguyenduchoc.bt5.dto.Models.Account;
import com.nguyenduchoc.bt5.dto.Models.Category;
import com.nguyenduchoc.bt5.dto.Models.Page;

@Repository
public class Store {
    private final JdbcTemplate jdbc;
    public Store(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private static final RowMapper<Account> ACCOUNT = (rs, i) -> new Account(rs.getInt("id"), rs.getString("username"),
            rs.getString("fullname"), rs.getString("email"), rs.getString("phone"), rs.getString("password"),
            rs.getInt("roleid"), rs.getBoolean("is_active"), rs.getObject("createddate", LocalDate.class));
    private static final RowMapper<Category> CATEGORY = (rs, i) -> new Category(rs.getInt("cate_id"),
            rs.getString("cate_name"), rs.getString("icons"));
    private static String pattern(String q) { return "%" + q.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"; }
    private static int pages(long total, int size) { return Math.max(1, (int) ((total + size - 1) / size)); }
    private static int page(int requested, int totalPages) { return Math.max(1, Math.min(requested, totalPages)); }
    public Account accountByUsername(String username) {
        try { return jdbc.queryForObject("SELECT * FROM dbo.[User] WHERE username = ?", ACCOUNT, username); }
        catch (EmptyResultDataAccessException e) { return null; }
    }
    public Account account(int id) {
        try { return jdbc.queryForObject("SELECT * FROM dbo.[User] WHERE id = ?", ACCOUNT, id); }
        catch (EmptyResultDataAccessException e) { return null; }
    }
    public boolean accountValueExists(String column, String value, int exceptId) {
        if (!List.of("username", "email", "phone").contains(column)) throw new IllegalArgumentException("Invalid column");
        return jdbc.queryForObject("SELECT COUNT(*) FROM dbo.[User] WHERE " + column + " = ? AND id <> ?", Long.class, value, exceptId) > 0;
    }
    public void createAccount(String username, String fullName, String email, String phone, String password, int role, boolean active) {
        jdbc.update("INSERT INTO dbo.[User](username,fullname,email,phone,password,roleid,is_active,createddate) VALUES(?,?,?,?,?,?,?,?)",
                username, fullName, email, phone, PasswordUtils.hash(password), role, active, LocalDate.now());
    }
    public void updateAccount(int id, String username, String fullName, String email, String phone, int role, boolean active, String password) {
        if (password == null || password.isBlank())
            jdbc.update("UPDATE dbo.[User] SET username=?,fullname=?,email=?,phone=?,roleid=?,is_active=? WHERE id=?",
                    username, fullName, email, phone, role, active, id);
        else jdbc.update("UPDATE dbo.[User] SET username=?,fullname=?,email=?,phone=?,roleid=?,is_active=?,password=? WHERE id=?",
                    username, fullName, email, phone, role, active, PasswordUtils.hash(password), id);
    }
    public void deleteAccount(int id) { jdbc.update("DELETE FROM dbo.[User] WHERE id=?", id); }
    public Page<Account> accounts(String q, int requested, int size) {
        String term = pattern(q == null ? "" : q);
        String where = " WHERE username LIKE ? ESCAPE '!' OR fullname LIKE ? ESCAPE '!' OR email LIKE ? ESCAPE '!' OR phone LIKE ? ESCAPE '!'";
        long total = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.[User]" + where, Long.class, term, term, term, term);
        int totalPages = pages(total, size), number = page(requested, totalPages);
        List<Account> items = jdbc.query("SELECT * FROM dbo.[User]" + where + " ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                ACCOUNT, term, term, term, term, (number - 1) * size, size);
        return new Page<>(items, number, totalPages, total);
    }
    public Category category(int id) {
        try { return jdbc.queryForObject("SELECT * FROM dbo.Category WHERE cate_id=?", CATEGORY, id); }
        catch (EmptyResultDataAccessException e) { return null; }
    }
    public void createCategory(String name, String icon) {
        jdbc.update("INSERT INTO dbo.Category(cate_name,icons) VALUES(?,?)", name, icon);
    }
    public void updateCategory(int id, String name, String icon) {
        jdbc.update("UPDATE dbo.Category SET cate_name=?,icons=? WHERE cate_id=?", name, icon, id);
    }
    public void deleteCategory(int id) { jdbc.update("DELETE FROM dbo.Category WHERE cate_id=?", id); }
    public Page<Category> categories(String q, int requested, int size) {
        String term = pattern(q == null ? "" : q);
        long total = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.Category WHERE cate_name LIKE ? ESCAPE '!'", Long.class, term);
        int totalPages = pages(total, size), number = page(requested, totalPages);
        List<Category> items = jdbc.query("SELECT * FROM dbo.Category WHERE cate_name LIKE ? ESCAPE '!' ORDER BY cate_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                CATEGORY, term, (number - 1) * size, size);
        return new Page<>(items, number, totalPages, total);
    }
}
