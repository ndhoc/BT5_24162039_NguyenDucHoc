package com.nguyenduchoc.bt5.repository;

import java.util.List;
import com.nguyenduchoc.bt5.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<Category, Integer> {
    default List<Category> getAll() {
        return JpaConfig.read(em -> em.createQuery(
                "select c from Category c order by c.id", Category.class).getResultList());
    }
    default Category get(int id) {
        return JpaConfig.read(em -> em.find(Category.class, id));
    }
    default Category get(String name) {
        return JpaConfig.read(em -> em.createQuery(
                        "select c from Category c where c.name = :name", Category.class)
                .setParameter("name", name)
                .getResultStream().findFirst().orElse(null));
    }
    default List<Category> search(String keyword) {
        // Treat SQL LIKE wildcards typed by the user as literal characters.
        String escaped = keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");
        return JpaConfig.read(em -> em.createQuery(
                        "select c from Category c where c.name like :keyword escape '!' order by c.id", Category.class)
                .setParameter("keyword", "%" + escaped + "%")
                .getResultList());
    }
    default void insert(Category category) {
        JpaConfig.write(em -> em.persist(category));
    }
    default void edit(Category category) {
        JpaConfig.write(em -> {
            Category managed = em.find(Category.class, category.getId());
            if (managed == null) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
            managed.setName(category.getName());
            managed.setIcon(category.getIcon());
        });
    }
    default void delete(int id) {
        JpaConfig.write(em -> {
            Category managed = em.find(Category.class, id);
            if (managed == null) throw new IllegalArgumentException("Danh mục không còn tồn tại.");
            em.remove(managed);
        });
    }
    default long count() {
        return JpaConfig.read(em -> em.createQuery("select count(c) from Category c", Long.class).getSingleResult());
    }
}
