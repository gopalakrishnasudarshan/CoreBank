package com.corebank.service;

import com.corebank.exception.*;
import com.corebank.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(User user) throws DuplicateUserException, DataAccessException, ValidationException;
    User getUserById(long id) throws NotFoundException, DataAccessException, ValidationException;
    User getUserByUsername(String username) throws NotFoundException, DataAccessException, ValidationException;
    List<User> getAllUsers() throws DataAccessException;
    User updateUser(User user) throws NotFoundException, DataAccessException, ValidationException,DuplicateUserException;
    void deleteUser(long userId) throws NotFoundException, DataAccessException, ValidationException, DuplicateUserException;
    User authenticate(String username, String password) throws AuthenticationException, DataAccessException, ValidationException;
    void changePassword(long userId, String oldPassword, String newPassword) throws NotFoundException, ValidationException, DataAccessException;

}
