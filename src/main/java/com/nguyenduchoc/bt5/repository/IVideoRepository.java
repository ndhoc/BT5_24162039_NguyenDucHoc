package com.nguyenduchoc.bt5.repository;

import java.util.List;
import com.nguyenduchoc.bt5.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IVideoRepository extends JpaRepository<Video, Integer> {
    default void insert(Video video) {
        JpaConfig.write(em -> em.persist(video));
    }
    default void update(Video video) {
        JpaConfig.write(em -> em.merge(video));
    }
    default void delete(int id) {
        JpaConfig.write(em -> {
            Video managed = em.find(Video.class, id);
            if (managed == null) throw new IllegalArgumentException("Video không còn tồn tại.");
            em.remove(managed);
        });
    }
    default Video get(int id) {
        return JpaConfig.read(em -> em.find(Video.class, id));
    }
    default List<Video> latest(int limit) {
        return JpaConfig.read(em -> em.createQuery(
                        "select v from Video v order by v.createdAt desc, v.id desc", Video.class)
                .setMaxResults(limit)
                .getResultList());
    }
    default List<Video> page(int offset, int limit) {
        return JpaConfig.read(em -> em.createQuery(
                        "select v from Video v order by v.createdAt desc, v.id desc", Video.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList());
    }
    default List<Video> pageByUser(int userId, int offset, int limit) {
        return JpaConfig.read(em -> em.createQuery(
                        "select v from Video v where v.user.id = :userId order by v.createdAt desc, v.id desc", Video.class)
                .setParameter("userId", userId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList());
    }
    default List<Video> all() {
        return JpaConfig.read(em -> em.createQuery(
                "select v from Video v order by v.createdAt desc, v.id desc", Video.class).getResultList());
    }
    default long countByUser(int userId) {
        return JpaConfig.read(em -> em.createQuery(
                        "select count(v) from Video v where v.user.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult());
    }
    default long count() {
        return JpaConfig.read(em -> em.createQuery("select count(v) from Video v", Long.class).getSingleResult());
    }
}
