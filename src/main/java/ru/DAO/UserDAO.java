package ru.DAO;

import java.util.List;

public interface UserDAO {

    void create(Object user);
    void update(Object user);
    Object getById(Long userId);
    List<Object> getAll(String s);
    void delete(Object user);

}
