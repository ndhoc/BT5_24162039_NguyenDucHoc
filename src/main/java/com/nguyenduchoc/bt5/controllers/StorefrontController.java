package com.nguyenduchoc.bt5.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import com.nguyenduchoc.bt5.entity.Product;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.entity.Video;
import com.nguyenduchoc.bt5.service.CategoryService;
import com.nguyenduchoc.bt5.service.ProductService;
import com.nguyenduchoc.bt5.service.UserService;
import com.nguyenduchoc.bt5.service.VideoService;

@Controller
public class StorefrontController {
    private final CategoryService categories;
    private final ProductService products;
    private final VideoService videos;
    private final UserService users;

    public StorefrontController(CategoryService categories, ProductService products, VideoService videos, UserService users) { this.categories = categories; this.products = products; this.videos = videos; this.users = users; }

    @GetMapping({"/", "/index", "/home"}) String home(Model model) {
        model.addAttribute("cateList", categories.getAll());
        model.addAttribute("productList", products.latest(6));
        model.addAttribute("videoList", videos.latest(10));
        return "legacy/index";
    }

    @GetMapping("/products") String products(@RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String q, Model model) {
        Integer selected = positiveOrNull(categoryId);
        model.addAttribute("cateList", categories.getAll());
        model.addAttribute("productList", products.search(selected, q));
        model.addAttribute("selectedCategoryId", selected);
        model.addAttribute("keyword", q == null ? "" : q.trim());
        return "legacy/products";
    }

    @GetMapping("/products/detail") String product(@RequestParam(required = false) String id, Model model) {
        Integer parsed = positiveOrNull(id);
        Product product = parsed == null ? null : products.get(parsed);
        if (product == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại.");
        model.addAttribute("product", product);
        return "legacy/product-detail";
    }

    @GetMapping("/video") String videoList(@RequestParam(required = false) String page, Model model) {
        Integer requested = positiveOrNull(page);
        int pageCount = videos.pageCount();
        int number = Math.min(requested == null ? 1 : requested, pageCount);
        model.addAttribute("videoList", videos.page(number));
        model.addAttribute("page", number);
        model.addAttribute("pageCount", pageCount);
        model.addAttribute("total", videos.count());
        return "legacy/video-list";
    }

    @GetMapping("/video/detail") String video(@RequestParam(required = false) String id, Model model) {
        Integer parsed = positiveOrNull(id);
        Video video = parsed == null ? null : videos.get(parsed);
        if (video == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video không tồn tại.");
        model.addAttribute("video", video);
        return "legacy/video-detail";
    }

    @GetMapping("/dashboard") String dashboard(Model model) {
        model.addAttribute("productCount", products.count());
        model.addAttribute("categoryCount", categories.count());
        model.addAttribute("userCount", users.count());
        model.addAttribute("totalValue", products.totalValue());
        model.addAttribute("categoryStats", products.countByCategory());
        model.addAttribute("recentProducts", products.latest(5));
        return "legacy/dashboard";
    }

    @GetMapping("/waiting") String waiting(HttpSession session) {
        User account = (User) session.getAttribute("account");
        if (account == null) return "redirect:/login";
        return account.getRoleid() == 1 || account.getRoleid() == 2 ? "redirect:/dashboard" : "redirect:/products";
    }

    private static Integer positiveOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { int value = Integer.parseInt(raw.trim()); return value > 0 ? value : null; }
        catch (NumberFormatException e) { return null; }
    }
}
