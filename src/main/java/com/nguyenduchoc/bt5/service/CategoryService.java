package com.nguyenduchoc.bt5.service;

import java.util.List;
import com.nguyenduchoc.bt5.entity.Category;

public interface CategoryService {
    void insert(Category category);
    void edit(Category category);
    void delete(int id);
    Category get(int id);
    Category get(String name);
    List<Category> getAll();
    List<Category> search(String keyword);
    long count();
}
