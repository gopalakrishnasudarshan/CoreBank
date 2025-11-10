package com.corebank.dao;

import com.corebank.model.User;

import java.util.List;

public interface UserDAO {

    void createUser(User user);
    User getUser(long id);
    List<User> getAllUsers();
    User getUserByUsername(String username);
    void updateUser(User user);
    void deleteUser(User user);




}
