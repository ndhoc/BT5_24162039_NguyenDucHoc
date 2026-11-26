package com.nguyenduchoc.bt5.repository;

import java.math.BigDecimal;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import com.nguyenduchoc.bt5.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProductRepository extends JpaRepository<Product, Integer> {
    default Product get(int id) {
        return JpaConfig.read(em -> em.find(Product.class, id));
    }
    default void insert(Product product) {
        JpaConfig.write(em -> em.persist(product));
    }
    default String update(Product changes) {
        return JpaConfig.writeResult(em -> {
            Product product = findForUpdate(em, changes.getId());
            product.setName(changes.getName());
            product.setPrice(changes.getPrice());
            product.setDescription(changes.getDescription());
            product.setCategory(changes.getCategory());
            if (changes.getImage() == null) return null;

            String replacedImage = product.getImage();
            product.setImage(changes.getImage());
            return replacedImage;
        });
    }
    default String delete(int id) {
        return JpaConfig.writeResult(em -> {
            Product product = findForUpdate(em, id);
            em.remove(product);
            return product.getImage();
        });
    }

    private Product findForUpdate(EntityManager em, int id) {
        Product product = em.find(Product.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (product == null) throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        return product;
    }
    default List<Product> latest(int limit) {
        return JpaConfig.read(em -> em.createQuery(
                        "select p from Product p order by p.id desc", Product.class)
                .setMaxResults(limit)
                .getResultList());
    }
    default List<Product> search(Integer categoryId, String keyword) {
        StringBuilder jpql = new StringBuilder("select p from Product p where 1 = 1");
        if (categoryId != null) jpql.append(" and p.category.id = :categoryId");
        if (keyword != null && !keyword.isBlank()) jpql.append(" and lower(p.name) like :keyword escape '!'");
        jpql.append(" order by p.id desc");

        String escaped = keyword == null ? null : keyword.toLowerCase()
                .replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");
        return JpaConfig.read(em -> {
            var query = em.createQuery(jpql.toString(), Product.class);
            if (categoryId != null) query.setParameter("categoryId", categoryId);
            if (escaped != null && !escaped.isBlank()) query.setParameter("keyword", "%" + escaped + "%");
            return query.getResultList();
        });
    }
    default long count() {
        return JpaConfig.read(em -> em.createQuery("select count(p) from Product p", Long.class).getSingleResult());
    }
    default BigDecimal totalValue() {
        return JpaConfig.read(em -> em.createQuery(
                        "select coalesce(sum(p.price), 0) from Product p", BigDecimal.class)
                .getSingleResult());
    }
    default List<Object[]> countByCategory() {
        return JpaConfig.read(em -> em.createQuery(
                        "select p.category.name, count(p) from Product p group by p.category.id, p.category.name order by p.category.name",
                        Object[].class)
                .getResultList());
    }
}
