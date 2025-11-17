package com.corebank.service.impl;

import com.corebank.dao.UserDAO;
import com.corebank.exception.*;
import com.corebank.model.User;
import com.corebank.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.OptionPaneUI;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


@Slf4j
public class UserServiceImpl implements UserService {

   private final UserDAO userDAO;
   Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

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

        //check if id is valid
        if (id <= 0) {
            throw new ValidationException("User Id cannot be zero or less than zero");
        }

        try {
            Optional<User> user = userDAO.getUser(id);

            if (user.isPresent()) {
                return user.get();
            } else {
                logger.warn("User with id " + id + " not found");
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
               logger.warn("User with name " + username + " not found");
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
            Optional<User> userExists = userDAO.getUser(user.getUserId());
            if(userExists.isEmpty())
            {
                throw new NotFoundException("User with id " + user.getUserId() + " not found");
            }
           Optional<User> userNameExists = userDAO.getUserByUsername(user.getUserName());
            if(userNameExists.isPresent() && userNameExists.get().getUserId() != user.getUserId())
            {
               throw new DuplicateUserException("Username "+user.getUserName()+" is already taken");
            }

            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);

            userDAO.updateUser(user);

            return user;


        } catch (SQLException e) {
            throw new DataAccessException("Error accessing database while updating user", e);
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error while updating user", e);
        }


    }

    @Override
    public void deleteUser(long userId) throws NotFoundException, DataAccessException,ValidationException,DuplicateUserException {

        if(userId <= 0)
        {
           throw  new ValidationException("User id cannot be 0 or less than zero");
        }

        try {
            Optional<User> userExists =userDAO.getUser(userId);
            if(userExists.isEmpty())
            {
                logger.warn("User with id " + userId + " not found");
            }
            userDAO.deleteUser(userExists.get());
            logger.info("User with id {} and username '{}' deleted successfully",
                    userExists.get().getUserId(), userExists.get().getUserName());

        } catch (SQLException e) {
            throw new DataAccessException("User with "+userId+" not found");
        } catch (Exception e) {
            throw new DataAccessException("Error while deleting a user data");
        }


    }


    @Override
    public User authenticate(String username, String password)
            throws AuthenticationException, ValidationException, DataAccessException {

        if (username == null || username.isBlank()) {
            throw new ValidationException("Username cannot be null or empty");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password cannot be null or empty");
        }

        try {
            Optional<User> optionalUser = userDAO.getUserByUsername(username);

            if (optionalUser.isEmpty()) {
                throw new AuthenticationException("Invalid username or password");
            }

            User user = optionalUser.get();

            if (!BCrypt.checkpw(password, user.getPassword())) {
                throw new AuthenticationException("Invalid username or password");
            }

            return user;

        } catch (SQLException e) {
            throw new DataAccessException("Error accessing database while authenticating user", e);
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error during authentication", e);
        }
    }


    @Override
    public void changePassword(long userId, String oldPassword, String newPassword)
            throws NotFoundException, ValidationException, DataAccessException {

        if (userId <= 0) {
            throw new ValidationException("User ID must be greater than zero");
        }
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new ValidationException("Old password cannot be empty");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password cannot be empty");
        }

        try {
            Optional<User> optionalUser = userDAO.getUser(userId);
            if (optionalUser.isEmpty()) {
                throw new NotFoundException("User with ID " + userId + " not found");
            }

            User user = optionalUser.get();

            if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
                throw new ValidationException("Old password is incorrect");
            }



            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);

            userDAO.updateUser(user);

        } catch (SQLException e) {
            throw new DataAccessException("Error accessing database while changing password", e);
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error while changing password", e);
        }
    }

}
