package com.nguyenduchoc.bt5.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.nguyenduchoc.bt5.entity.Product;
import com.nguyenduchoc.bt5.repository.IProductRepository;
import com.nguyenduchoc.bt5.service.ProductService;

@org.springframework.stereotype.Service
public class ProductServiceImpl implements ProductService {
    private final IProductRepository repository;

    public ProductServiceImpl(IProductRepository repository) { this.repository = repository; }

    @Override public Product get(int id) { return repository.get(id); }
    @Override public void insert(Product product) { repository.insert(product); }
    @Override public String update(Product product) { return repository.update(product); }
    @Override public String delete(int id) { return repository.delete(id); }
    @Override public List<Product> latest(int limit) { return repository.latest(limit); }
    @Override public List<Product> search(Integer categoryId, String keyword) { return repository.search(categoryId, keyword); }
    @Override public long count() { return repository.count(); }
    @Override public BigDecimal totalValue() { return repository.totalValue(); }
    @Override public List<Object[]> countByCategory() { return repository.countByCategory(); }
}
