package com.corebank.dao;

import com.corebank.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountDAO {

    void addAccount(Account account) throws SQLException;
    void addAccount(Account account, Connection connection) throws SQLException;

    Optional<Account> getAccountById(long accountId) throws SQLException;
    Optional<Account> getAccountById(long accountId, Connection connection) throws SQLException;


    List<Account> getAccountsByCustomerId(long customerId) throws SQLException;
    List<Account> getAccountsByCustomerId(long customerId, Connection connection) throws SQLException;


    void updateBalance(long accountId, BigDecimal newBalance) throws SQLException;
    void updateBalance(long accountId, BigDecimal newBalance, Connection connection) throws SQLException;


    void deleteAccount(long accountId) throws SQLException;
    void deleteAccount(long accountId, Connection connection) throws SQLException;
}

