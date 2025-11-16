package com.corebank.service;

import com.corebank.exception.DataAccessException;
import com.corebank.exception.DuplicateUserException;
import com.corebank.exception.NotFoundException;
import com.corebank.exception.ValidationException;
import com.corebank.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(User user) throws DuplicateUserException, DataAccessException, ValidationException;
    User getUserById(long id) throws NotFoundException, DataAccessException, ValidationException;
    User getUserByUsername(String username) throws NotFoundException, DataAccessException, ValidationException;
    List<User> getAllUsers() throws DataAccessException;
    User updateUser(User user) throws NotFoundException, DataAccessException, ValidationException;
    void deleteUser(long userId) throws NotFoundException, DataAccessException;
}
