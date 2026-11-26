package com.nguyenduchoc.bt5.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import com.nguyenduchoc.bt5.dto.Models.Category;
import com.nguyenduchoc.bt5.jdbc.Store;

@Controller
public class LegacyCategoryRoutes {
    private final Store store;
    public LegacyCategoryRoutes(Store store) { this.store = store; }
    @GetMapping("/admin/category/list") String list() { return "redirect:/admin/category"; }
    @GetMapping("/admin/category/add") String add() { return "redirect:/admin/category/new"; }
    @GetMapping("/admin/category/delete") String confirm(@RequestParam int id, Model model) {
        Category category = store.category(id);
        if (category == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("category", category);
        return "legacy/admin/delete-category";
    }
}
