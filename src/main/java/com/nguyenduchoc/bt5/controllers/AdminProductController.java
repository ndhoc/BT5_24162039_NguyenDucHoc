package com.nguyenduchoc.bt5.controllers;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.nguyenduchoc.bt5.entity.Category;
import com.nguyenduchoc.bt5.entity.Product;
import com.nguyenduchoc.bt5.service.CategoryService;
import com.nguyenduchoc.bt5.service.ProductService;
import com.nguyenduchoc.bt5.utils.ImageStorage;
import com.nguyenduchoc.bt5.validation.FormValidation;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@Controller
public class AdminProductController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminProductController.class);
    private final ProductService products;
    private final CategoryService categories;

    public AdminProductController(ProductService products, CategoryService categories) { this.products = products; this.categories = categories; }

    @GetMapping({"/admin/product", "/admin/product/list"}) String list(HttpServletRequest req) {
        String keyword = req.getParameter("q");
        req.setAttribute("productList", products.search(null, keyword == null ? null : keyword.trim()));
        return "legacy/admin/list-product";
    }
    @GetMapping("/admin/product/add") String add(HttpServletRequest req) { return form(req, false); }
    @GetMapping("/admin/product/edit") String edit(HttpServletRequest req) {
        req.setAttribute("product", existing(req));
        return form(req, true);
    }
    @GetMapping("/admin/product/delete") String confirmDelete(HttpServletRequest req) {
        req.setAttribute("product", existing(req));
        return "legacy/admin/delete-product";
    }
    @PostMapping("/admin/product/add") String addPost(HttpServletRequest req,
            @RequestParam(required = false) MultipartFile image) throws IOException { return save(req, image, false); }
    @PostMapping("/admin/product/edit") String editPost(HttpServletRequest req,
            @RequestParam(required = false) MultipartFile image) throws IOException { return save(req, image, true); }
    @PostMapping("/admin/product/delete") String delete(HttpServletRequest req) {
        cleanup(products.delete(productId(req)));
        return "redirect:/admin/product/list?saved=delete";
    }

    private String save(HttpServletRequest req, MultipartFile image, boolean editing) throws IOException {
        Product changes = new Product();
        if (editing) {
            Product current = existing(req);
            req.setAttribute("product", current);
            changes.setId(current.getId());
        }
        String uploaded = null;
        boolean saved = false;
        try {
            changes.setName(FormValidation.requiredText("name", req.getParameter("name"), 255, "Tên sản phẩm"));
            changes.setPrice(FormValidation.productPrice(req.getParameter("price")));
            changes.setDescription(FormValidation.description(req.getParameter("description")));
            int categoryId = FormValidation.positiveId("categoryId", req.getParameter("categoryId"), "Vui lòng chọn danh mục hợp lệ.");
            Category category = categories.get(categoryId);
            if (category == null) throw new FieldError("categoryId", "Danh mục không tồn tại.");
            changes.setCategory(category);
            uploaded = ImageStorage.saveProduct(image);
            changes.setImage(uploaded);
            String replaced = editing ? products.update(changes) : null;
            if (!editing) products.insert(changes);
            saved = true;
            cleanup(replaced);
            return "redirect:/admin/product/list?saved=" + (editing ? "edit" : "add");
        } catch (IllegalArgumentException e) {
            FormErrors.show(req, e);
            req.setAttribute("submitted", true);
            return form(req, editing);
        } finally {
            if (!saved) cleanup(uploaded);
        }
    }
    private String form(HttpServletRequest req, boolean editing) {
        req.setAttribute("editing", editing);
        req.setAttribute("categoryList", categories.getAll());
        return "legacy/admin/product-form";
    }
    private Product existing(HttpServletRequest req) {
        Product product = products.get(productId(req));
        if (product == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại.");
        return product;
    }
    private static int productId(HttpServletRequest req) {
        try { return FormValidation.positiveId("id", req.getParameter("id"), "Mã sản phẩm không hợp lệ."); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()); }
    }
    private void cleanup(String image) {
        try { ImageStorage.deleteProduct(image); }
        catch (IOException e) { LOG.warn("Could not remove product image {}", image, e); }
    }
}
