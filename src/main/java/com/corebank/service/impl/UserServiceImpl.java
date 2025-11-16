package com.corebank.service.impl;

import com.corebank.dao.UserDAO;
import com.corebank.exception.DataAccessException;
import com.corebank.exception.DuplicateUserException;
import com.corebank.exception.NotFoundException;
import com.corebank.exception.ValidationException;
import com.corebank.model.User;
import com.corebank.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Slf4j
public class UserServiceImpl implements UserService {

   private final UserDAO userDAO;
   Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User createUser(User user) throws DuplicateUserException, DataAccessException, ValidationException {

        //validating the input values

        if(user == null)
        {
           throw new ValidationException("User cannot be null");
        }
        if(user.getUserName() == null || user.getUserName().isBlank())
        {
            throw new ValidationException("UserName cannot be null or empty");
        }
        if(user.getPassword() == null || user.getPassword().isBlank())
        {
            throw new ValidationException("Password cannot be null or empty");
        }
        if(user.getRole() == null)
        {
            throw new ValidationException("Role cannot be null");
        }

        try {
            Optional<User> existing = userDAO.getUserByUsername(user.getUserName());
            if(existing.isPresent())
            {
                throw new DuplicateUserException("Username "+user.getUserName()+" is already taken");
            }

            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);

            userDAO.createUser(user);
        } catch (SQLException e) {
            throw new DataAccessException("Error accessing Database while creating user", e);
        }catch (Exception e) {
            throw new DataAccessException("Unexpected error while creating user", e);
        }


        return user;
    }

    @Override
    public User getUserById(long id) throws NotFoundException, DataAccessException, ValidationException {

        if (id <= 0) {
            throw new ValidationException("User Id cannot be zero or less than zero");
        }

        try {
            Optional<User> user = userDAO.getUser(id);

            if (user.isPresent()) {
                return user.get();
            } else {
                logger.warning("User with id " + id + " not found");
                throw new NotFoundException("User with id " + id + " not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error accessing Database while getting user", e);
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error while getting user", e);
        }

    }

    @Override
    public User getUserByUsername(String username) throws NotFoundException, DataAccessException, ValidationException {

        if( username == null || username.isBlank())
        {
            throw new ValidationException("Username cannot be null or empty");
        }
        try {
            username = username.trim();
           Optional<User> user =  userDAO.getUserByUsername(username);

           if(user.isPresent())
           {
               return user.get();
           }
           else {
               logger.warning("User with name " + username + " not found");
               throw new NotFoundException("User with name " + username + " not found");
           }
        } catch (SQLException e) {
            throw new DataAccessException("Error accessing Database while getting user", e);
        }catch (Exception e) {
            throw new DataAccessException("Unexpected error while getting user", e);
        }

    }

    @Override
    public List<User> getAllUsers() throws DataAccessException {

        try {
            List<User> users = userDAO.getAllUsers();
            logger.info("Fetched {} users from the database: "+users.size());
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all users from the DB",e);
        }catch (Exception e) {
            throw new DataAccessException("Unexpected error while getting the users");
        }

    }

    @Override
    public User updateUser(User user) throws NotFoundException, DataAccessException,ValidationException {

        if(user == null)
        {
            throw new ValidationException("User cannot be null");
        }

        if(user.getUserId() <= 0)
        {
            throw new ValidationException("UserId cannot be zero or less than zero");
        }
        if(user.getUserName() == null || user.getUserName().isBlank())
        {
            throw new ValidationException("UserName cannot be null or empty");
        }
        if(user.getPassword() == null || user.getPassword().isBlank())
        {
            throw new ValidationException("Password cannot be null or empty");
        }
        if(user.getRole() == null)
        {
            throw new ValidationException("Role cannot be null");
        }

        try {
            userDAO.getUser(user.getUserId());
        } catch (SQLException e) {
            throw new NotFoundException("User with id: "+user.getUserId()+"not found");
        }

        return null;
    }

    @Override
    public void deleteUser(long userId) throws NotFoundException, DataAccessException {

    }
}
