package com.nguyenduchoc.bt5.controllers;

import com.nguyenduchoc.bt5.jdbc.Store;
import com.nguyenduchoc.bt5.validation.Validation;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.nguyenduchoc.bt5.dto.Models.Account;
import com.nguyenduchoc.bt5.dto.Models.Category;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.utils.ImageStorage;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);
    private final Store store;
    public AdminController(Store store) { this.store = store; }
    @GetMapping("/category") public String categories(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("result", store.categories(q, page, 10)); model.addAttribute("q", q); return "admin/categories";
    }
    @GetMapping("/category/new") public String categoryNew() { return "admin/category-form"; }
    @GetMapping("/category/edit") public String categoryEdit(@RequestParam int id, Model model) {
        Category category = store.category(id);
        if (category == null) return "redirect:/admin/category?missing=1";
        model.addAttribute("category", category); return "admin/category-form";
    }
    @PostMapping("/category/save") public String categorySave(@RequestParam(defaultValue = "0") int id,
            @RequestParam String name, @RequestParam(defaultValue = "") String icon,
            @RequestParam(required = false) MultipartFile iconFile, Model model) throws IOException {
        name = name.trim(); icon = icon.trim();
        model.addAttribute("category", new Category(id, name, icon));
        if (name.isBlank() || name.length() > 255 || icon.length() > 255) {
            model.addAttribute("error", "Tên danh mục phải có 1–255 ký tự; đường dẫn ảnh tối đa 255 ký tự."); return "admin/category-form";
        }
        Category previous = id == 0 ? null : store.category(id);
        if (id != 0 && previous == null) return "redirect:/admin/category?missing=1";
        String uploaded = null;
        boolean saved = false;
        try {
            uploaded = ImageStorage.save(iconFile);
            String storedIcon = uploaded == null ? (icon.isEmpty() ? null : icon) : uploaded;
            if (id == 0) store.createCategory(name, storedIcon);
            else store.updateCategory(id, name, storedIcon);
            saved = true;
            if (uploaded != null && previous != null) {
                try { ImageStorage.delete(previous.icon()); }
                catch (IOException e) { LOG.warn("Could not remove old category image {}", previous.icon(), e); }
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage()); return "admin/category-form";
        } finally {
            if (!saved && uploaded != null) ImageStorage.delete(uploaded);
        }
        return "redirect:/admin/category?saved=1";
    }
    @PostMapping("/category/delete") public String categoryDelete(@RequestParam int id, Model model) {
        Category target = store.category(id);
        if (target == null) return "redirect:/admin/category?missing=1";
        try { store.deleteCategory(id); }
        catch (DataIntegrityViolationException e) { return "redirect:/admin/category?inUse=1"; }
        try { ImageStorage.delete(target.icon()); }
        catch (IOException e) { LOG.warn("Could not remove category image {}", target.icon(), e); }
        return "redirect:/admin/category?deleted=1";
    }
    @GetMapping("/user") public String users(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("result", store.accounts(q, page, 10)); model.addAttribute("q", q); return "admin/users";
    }
    @GetMapping("/user/new") public String userNew() { return "admin/user-form"; }
    @GetMapping("/user/edit") public String userEdit(@RequestParam int id, Model model) {
        Account account = store.account(id);
        if (account == null) return "redirect:/admin/user?missing=1";
        model.addAttribute("user", account); return "admin/user-form";
    }
    @PostMapping("/user/save") public String userSave(@RequestParam(defaultValue = "0") int id,
            @RequestParam String username, @RequestParam String fullName, @RequestParam String email,
            @RequestParam String phone, @RequestParam String password, @RequestParam String confirm,
            @RequestParam int roleId, @RequestParam(defaultValue = "false") boolean active,
            HttpSession session, Model model) {
        username = username.trim(); fullName = fullName.trim(); email = email.trim(); phone = phone.trim();
        model.addAttribute("formId", id); model.addAttribute("username", username); model.addAttribute("fullName", fullName);
        model.addAttribute("email", email); model.addAttribute("phone", phone); model.addAttribute("roleId", roleId);
        model.addAttribute("active", active);
        String error = Validation.account(username, fullName, email, phone, password, confirm, id == 0);
        if (error == null && roleId != 1 && roleId != 2 && roleId != 5) error = "Vai trò không hợp lệ.";
        if (error == null && id > 0 && store.account(id) == null) error = "Tài khoản không tồn tại.";
        User current = (User) session.getAttribute("account");
        if (error == null && current != null && id == current.getId() && (roleId != 1 || !active))
            error = "Không thể bỏ quyền admin hoặc khóa chính tài khoản đang dùng.";
        if (error == null) error = Validation.duplicates(store, username, email, phone, id);
        if (error != null) { model.addAttribute("error", error); return "admin/user-form"; }
        try {
            if (id == 0) store.createAccount(username, fullName, email, phone, password, roleId, active);
            else store.updateAccount(id, username, fullName, email, phone, roleId, active, password);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Tên đăng nhập, email hoặc số điện thoại đã tồn tại."); return "admin/user-form";
        }
        return "redirect:/admin/user?saved=1";
    }
    @PostMapping("/user/delete") public String userDelete(@RequestParam int id, HttpSession session) {
        User current = (User) session.getAttribute("account");
        if (current != null && id == current.getId()) return "redirect:/admin/user?self=1";
        if (store.account(id) == null) return "redirect:/admin/user?missing=1";
        try { store.deleteAccount(id); }
        catch (DataIntegrityViolationException e) { return "redirect:/admin/user?inUse=1"; }
        return "redirect:/admin/user?deleted=1";
    }
}
