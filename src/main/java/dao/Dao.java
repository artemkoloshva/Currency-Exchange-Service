package dao;

import java.util.List;

public interface Dao<E> {
    E create(E entity);
    E read(int id);
    List<E> readAll();
    E update(E entity);
    void delete(int id);
}
