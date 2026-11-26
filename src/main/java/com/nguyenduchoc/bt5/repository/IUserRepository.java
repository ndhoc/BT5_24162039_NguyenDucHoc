package com.nguyenduchoc.bt5.repository;

import java.time.Instant;
import java.util.List;
import com.nguyenduchoc.bt5.entity.RememberToken;
import com.nguyenduchoc.bt5.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Integer> {
    default User get(String username) {
        return JpaConfig.read(em -> em.createQuery(
                        "select u from AppUser u where u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream().findFirst().orElse(null));
    }
    default User getById(int id) {
        return JpaConfig.read(em -> em.find(User.class, id));
    }
    default User getByEmail(String email) {
        return JpaConfig.read(em -> em.createQuery(
                        "select u from AppUser u where u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream().findFirst().orElse(null));
    }
    default List<User> all() {
        return JpaConfig.read(em -> em.createQuery(
                "select u from AppUser u order by u.id", User.class).getResultList());
    }
    default void insert(User user) {
        JpaConfig.write(em -> em.persist(user));
    }
    default void update(User user) {
        JpaConfig.write(em -> em.merge(user));
    }

    private boolean exists(String jpql, String value) {
        return JpaConfig.read(em -> em.createQuery(jpql, Long.class)
                .setParameter("value", value)
                .getSingleResult() > 0);
    }
    default boolean checkExistUsername(String value) {
        return exists("select count(u) from AppUser u where u.username = :value", value);
    }
    default boolean checkExistEmail(String value) {
        return exists("select count(u) from AppUser u where u.email = :value", value);
    }
    default boolean checkExistPhone(String value) {
        return exists("select count(u) from AppUser u where u.phone = :value", value);
    }
    default void saveRememberToken(int userId, String tokenHash, Instant expires) {
        JpaConfig.write(em -> em.persist(new RememberToken(tokenHash, em.getReference(User.class, userId), expires)));
    }
    default User findByRememberToken(String tokenHash) {
        return JpaConfig.read(em -> em.createQuery(
                        "select t.user from RememberToken t where t.tokenHash = :hash and t.expiresAt > :now", User.class)
                .setParameter("hash", tokenHash)
                .setParameter("now", Instant.now())
                .getResultStream().findFirst().orElse(null));
    }
    default void deleteRememberToken(String tokenHash) {
        JpaConfig.write(em -> em.createQuery("delete from RememberToken t where t.tokenHash = :hash")
                .setParameter("hash", tokenHash)
                .executeUpdate());
    }
    default long count() {
        return JpaConfig.read(em -> em.createQuery("select count(u) from AppUser u", Long.class).getSingleResult());
    }
}
