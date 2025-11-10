package com.corebank.dao;

import com.corebank.model.User;

import javax.swing.text.html.Option;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDAO {

    void createUser(User user);
    Optional<User> getUser(long id) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    Optional<User> getUserByUsername(String username) throws SQLException;
    void updateUser(User user) throws SQLException;
    void deleteUser(User user);




}
