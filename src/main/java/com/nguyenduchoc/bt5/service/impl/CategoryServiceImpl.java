package com.nguyenduchoc.bt5.service.impl;

import java.util.List;

import com.nguyenduchoc.bt5.entity.Category;
import com.nguyenduchoc.bt5.repository.ICategoryRepository;
import com.nguyenduchoc.bt5.service.CategoryService;
import com.nguyenduchoc.bt5.validation.FormValidation;

@org.springframework.stereotype.Service
public class CategoryServiceImpl implements CategoryService {
    private final ICategoryRepository repository;

    public CategoryServiceImpl(ICategoryRepository repository) { this.repository = repository; }

    private void validate(Category category) {
        category.setName(FormValidation.requiredText("catename", category.getName(), 255, "Tên danh mục"));
    }

    @Override public void insert(Category category) { validate(category); repository.insert(category); }
    @Override public void edit(Category category) { validate(category); repository.edit(category); }
    @Override public void delete(int id) { repository.delete(id); }
    @Override public Category get(int id) { return repository.get(id); }
    @Override public Category get(String name) { return repository.get(name); }
    @Override public List<Category> getAll() { return repository.getAll(); }
    @Override public List<Category> search(String keyword) { return keyword == null || keyword.isBlank() ? getAll() : repository.search(keyword.trim()); }
    @Override public long count() { return repository.count(); }
}
