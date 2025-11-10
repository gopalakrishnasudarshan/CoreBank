package com.corebank.dao.impl;

import com.corebank.dao.UserDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    @Override
    public void createUser(User user) {

       String sql = "INSERT INTO users (username, password_hash,role) VALUES (?,?,?)";
       try(Connection connection = DBConnectionManager.getInstance().getConnection();
          PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){

           preparedStatement.setString(1,user.getUserName());
           preparedStatement.setString(2,user.getPassword());
           preparedStatement.setString(3,user.getRole().name());

           int rowsAffected = preparedStatement.executeUpdate();

           if(rowsAffected > 0){
               try (ResultSet resultSet = preparedStatement.getGeneratedKeys()){
                   if(resultSet.next())
                   {
                       user.setUserId(resultSet.getLong(1));
                   }

               }

           }
           System.out.println("User created Successfully");

       } catch (SQLException e) {
          e.printStackTrace();
       }


    }

    @Override
    public User getUser(long id) {
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return List.of();
    }

    @Override
    public User getUserByUsername(String username) {
        return null;
    }

    @Override
    public void updateUser(User user) {

    }

    @Override
    public void deleteUser(User user) {

    }
}
