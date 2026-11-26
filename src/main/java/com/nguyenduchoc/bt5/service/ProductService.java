package com.nguyenduchoc.bt5.service;

import java.math.BigDecimal;
import java.util.List;

import com.nguyenduchoc.bt5.entity.Product;

public interface ProductService {
    Product get(int id);
    void insert(Product product);
    String update(Product product);
    String delete(int id);
    List<Product> latest(int limit);
    List<Product> search(Integer categoryId, String keyword);
    long count();
    BigDecimal totalValue();
    List<Object[]> countByCategory();
}
