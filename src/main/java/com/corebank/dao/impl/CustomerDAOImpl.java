package com.corebank.dao.impl;

import com.corebank.dao.CustomerDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {

    Logger logger = LoggerFactory.getLogger(CustomerDAOImpl.class);

    @Override
    public Customer registerCustomer(Customer customer) {

        String sql = "INSERT INTO customers(first_name,last_name,dob,email,phone,address) VALUES (?,?,?,?,?,?)";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, customer.getFirstName());
            preparedStatement.setString(2, customer.getLastName());

            LocalDate birthDate = customer.getBirthDate();

            if (birthDate != null) {
                preparedStatement.setDate(3, java.sql.Date.valueOf(birthDate));
            } else {
                preparedStatement.setNull(3, java.sql.Types.DATE);
            }
            preparedStatement.setString(4, customer.getEmail());
            preparedStatement.setString(5, customer.getPhone());
            preparedStatement.setString(6, customer.getAddress());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        customer.setCustomerId(resultSet.getLong(1));
                    }
                }

            }
            logger.info("Customer registered Successfully");


        } catch (SQLException e) {
            logger.error("Error registering the Customer", e);
            return null;
        }
        return customer;
    }

    @Override
    public Optional<Customer> getCustomerById(long id) {

        String sql = "SELECT customer_id, first_name,last_name,dob,email,phone,address,created_at FROM customers " +
                "WHERE customer_id = ?";
        if (id <= 0) {
            return Optional.empty();
        }

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    long customer_id = resultSet.getLong("customer_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");

                    Date date = resultSet.getDate("dob");
                    LocalDate dob = (date != null) ? date.toLocalDate() : null;

                    String email = resultSet.getString("email");
                    String phone = resultSet.getString("phone");
                    String address = resultSet.getString("address");

                    Timestamp ts = resultSet.getTimestamp("created_at");
                    LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

                    Customer customer = new Customer(customer_id, firstName, lastName, dob, email, phone, address, createdAt);
                    return Optional.of(customer);

                }

            }

        } catch (SQLException e) {
            logger.error("Cannot find the customer with customer_id {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> getAllCustomers() {

        String sql = "SELECT customer_id, first_name, last_name, dob, email, phone, address,created_at FROM customers";
        List<Customer> customers = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    long customer_id = resultSet.getLong("customer_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");

                    Date date = resultSet.getDate("dob");
                    LocalDate dob = (date != null) ? date.toLocalDate() : null;

                    String email = resultSet.getString("email");
                    String phone = resultSet.getString("phone");
                    String address = resultSet.getString("address");

                    Timestamp ts = resultSet.getTimestamp("created_at");
                    LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

                    Customer customer = new Customer(customer_id, firstName, lastName, dob, email, phone, address, createdAt);
                    customers.add(customer);

                }
            }

        } catch (SQLException e) {
            logger.warn("No customers found", e);

        }

        return customers;
    }

    @Override
    public void updateCustomer(Customer customer) {

        String sql = "UPDATE customers\n" +
                "SET first_name = ?, last_name = ?, dob = ?, email = ?, phone = ?, address = ?\n" +
                "WHERE customer_id = ?\n";


        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, customer.getFirstName());
            preparedStatement.setString(2, customer.getLastName());

            LocalDate date = customer.getBirthDate();
            if (date != null) {
                preparedStatement.setDate(3, java.sql.Date.valueOf(date));
            } else {
                preparedStatement.setNull(3, java.sql.Types.DATE);
            }

            preparedStatement.setString(4, customer.getEmail());
            preparedStatement.setString(5, customer.getPhone());
            preparedStatement.setString(6, customer.getAddress());
            preparedStatement.setLong(7, customer.getCustomerId());


            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Customer Updated Successfully");
            } else {
                logger.warn("Customer with customer_id {}, not found", customer.getCustomerId());
            }
        } catch (SQLException e) {

            logger.error("Error updating customer with customer_id {}: {}", customer.getCustomerId(), e.getMessage(), e);
        }

    }

    @Override
    public void deleteCustomer(long id) {

        if (id <= 0) {
            logger.warn("Invalid customer id: {}", id);
            return;
        }

        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            int rowAffected = preparedStatement.executeUpdate();

            if (rowAffected > 0) {
                logger.info("Customer with customer_id {}:  Deleted Successfully", id);
            } else {
                logger.warn("Customer with customer_id {}, not found", id);
            }
        } catch (SQLException e) {
            logger.error("Error deleting customer with customer_id {}: {}", id, e.getMessage(), e);
        }


    }

    @Override
    public List<Customer> getCustomerByName(String name) {

        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }

        String sql = "SELECT customer_id, first_name, last_name, dob, email, phone, address,created_at FROM customers where first_name =  LIKE ?";
        List<Customer> customers = new ArrayList<>();

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, name + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {

                    long customer_id = resultSet.getLong("customer_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");

                    Date date = resultSet.getDate("dob");
                    LocalDate dob = (date != null) ? date.toLocalDate() : null;

                    String email = resultSet.getString("email");
                    String phone = resultSet.getString("phone");
                    String address = resultSet.getString("address");

                    Timestamp ts = resultSet.getTimestamp("created_at");
                    LocalDateTime created_at = (ts != null) ? ts.toLocalDateTime() : null;


                    Customer customer = new Customer(customer_id, firstName, lastName, dob, email, phone, address, created_at);
                    customers.add(customer);
                }

            }

        } catch (SQLException e) {
            logger.warn("Error fetching customer with first_name {}", name, e.getMessage(), e);
        }


        return customers;
    }

    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT customer_id, first_name, last_name, dob, email, phone, address,created_at FROM customers where email = ?";


        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {

                    long customer_id = resultSet.getLong("customer_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");

                    Date date = resultSet.getDate("dob");
                    LocalDate dob = (date != null) ? date.toLocalDate() : null;

                    String eMail = resultSet.getString("email");
                    String phone = resultSet.getString("phone");
                    String address = resultSet.getString("address");

                    Timestamp ts = resultSet.getTimestamp("created_at");
                    LocalDateTime created_at = (ts != null) ? ts.toLocalDateTime() : null;


                    Customer customer = new Customer(customer_id, firstName, lastName, dob, eMail, phone, address, created_at);
                   return Optional.of(customer);
                }


            }

        } catch (SQLException e) {
            logger.warn("Error fetching customer with email{}: {}", email, e.getMessage(), e);
        }

       return Optional.empty();

    }
}
