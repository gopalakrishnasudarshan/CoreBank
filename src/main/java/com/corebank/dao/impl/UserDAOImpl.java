package com.corebank.dao.impl;

import com.corebank.dao.UserDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserDAOImpl implements UserDAO {

    Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public void createUser(User user) {

        String sql = "INSERT INTO users (username, password_hash,role) VALUES (?,?,?)";
        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getRole().name());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        user.setUserId(resultSet.getLong(1));
                    }

                }

            }
            logger.info("User created Successfully");

        } catch (SQLException e) {
            logger.error("Error creating user '{}': {}", user.getUserName(), e.getMessage(), e);
        }


    }

    @Override
    public Optional<User> getUser(long id) throws SQLException {

        String sql = "SELECT user_id, username, password_hash, role, created_at FROM users WHERE user_id = ? LIMIT 1";
        if (id <= 0) {
            return Optional.empty();
        }


        try (Connection connection = DBConnectionManager.getInstance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    long userId = resultSet.getLong("user_id");
                    String userName = resultSet.getString("username");
                    String password = resultSet.getString("password_hash");

                    Timestamp ts = resultSet.getTimestamp("created_at");
                    LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

                    String roleStr = resultSet.getString("role");
                    User.Role role = null;
                    if (roleStr != null) {
                        try {
                            role = User.Role.valueOf(roleStr.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {

                            logger.warn("Invalid role value for user_id {}: '{}'", userId, roleStr);
                        }

                    }
                    User user = new User(userId, userName, password, role, createdAt);
                    return Optional.of(user);

                } else {
                    return Optional.empty();
                }
            }

        }


    }

    @Override
    public List<User> getAllUsers() throws SQLException {

        String sql = "SELECT *FROM users";
        List<User> users = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getInstance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    long userId = resultSet.getLong("user_id");
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password_hash");

                    String roleStr = resultSet.getString("role");
                    User.Role role = null;
                    if (roleStr != null) {
                        try {
                            role = User.Role.valueOf(roleStr.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {

                            logger.warn("Invalid role for user_id {}: '{}'", userId, roleStr);
                        }

                    }

                    Timestamp ts = resultSet.getTimestamp("created_at");
                    LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

                    User user = new User(userId, username, password, role, createdAt);
                    users.add(user);


                }

            }


        }


        return users;
    }

    @Override
    public Optional<User> getUserByUsername(String username) throws SQLException {

        if (username == null || username.isBlank()) return Optional.empty();

        String sql = "SELECT user_id , username, password_hash, role, created_at FROM users WHERE username = ? LIMIT 1";

        try (Connection connection = DBConnectionManager.getInstance().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    long userId = resultSet.getLong("user_id");
                    String userName = resultSet.getString("username");
                    String password = resultSet.getString("password_hash");

                    String roleStr = resultSet.getString("role");
                    User.Role role = null;
                    if (roleStr != null) {
                        try {
                            role = User.Role.valueOf(roleStr.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            logger.warn("Invalid role for user_name {}: '{}'", userName, roleStr);

                        }


                    }
                    Timestamp ts = resultSet.getTimestamp("created_at");
                    LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

                    User user = new User(userId, userName, password, role, createdAt);
                    return Optional.of(user);
                }
                return Optional.empty();
            }

        }


    }

    @Override
    public void updateUser(User user) throws SQLException {

        String sql = "UPDATE users SET username = ?, password_hash = ?, role = ? WHERE user_id = ?";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getPassword());
            String roleName = (user.getRole() != null) ? user.getRole().name() : null;
            preparedStatement.setString(3, roleName);
            preparedStatement.setLong(4, user.getUserId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User Updated Successfully");
            } else {
                logger.warn("User with user_id {}, not found", user.getUserId());
            }
        } catch (SQLException e) {

            logger.error("Error updating user with user_id {}: {}", user.getUserId(), e.getMessage(), e);
        }

    }

    @Override
    public void deleteUser(User user) {

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, user.getUserId());
            int rowAffected = preparedStatement.executeUpdate();

            if (rowAffected > 0) {
                logger.info("User with user_id {}: '{}' Deleted Successfully", user.getUserId(), user.getUserName());
            } else {
                logger.warn("User with user_id {}, not found", user.getUserId());
            }
        } catch (SQLException e) {
            logger.error("Error deleting user with user_id {}: {}", user.getUserId(), e.getMessage(), e);
        }


    }
}
