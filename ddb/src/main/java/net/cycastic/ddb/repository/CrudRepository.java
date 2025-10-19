package net.cycastic.ddb.repository;

import java.util.Optional;

public interface CrudRepository<T, PK, SK> {
    void save(T entity);
    void create(T entity);
    Optional<T> find(PK pk, SK sk);
    void delete(PK pk, SK sk);
}
