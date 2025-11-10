package com.corebank.dao;

import com.corebank.model.Customer;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO {

    Customer registerCustomer(Customer customer);
    Optional<Customer> getCustomerById(long id);
    List<Customer> getAllCustomers();
    void updateCustomer(Customer customer);
    void deleteCustomer(long customerId);
    List<Customer> getCustomerByName(String name);
    Optional<Customer> getCustomerByEmail(String email);



}
