package com.corebank.dao.impl;

import com.corebank.dao.AccountDAO;
import com.corebank.dao.CustomerDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.model.Account;
import com.corebank.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AccountDAOImpl implements AccountDAO {


    CustomerDAO customerDAO = new CustomerDAOImpl();
    Logger logger = LoggerFactory.getLogger(AccountDAOImpl.class);

    //helper function , because in the Account model we have A customer Object.
    private Account mapRowToAccount(ResultSet resultSet) throws SQLException {

        long accountId = resultSet.getLong("account_id");
        long customerId = resultSet.getLong("customer_id");
        String accountTypeStr = resultSet.getString("account_type");
        BigDecimal balance = resultSet.getBigDecimal("balance");
        String statusStr = resultSet.getString("status");
        Timestamp ts = resultSet.getTimestamp("ts");
        LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

        Customer customer = customerDAO.getCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found for id:" + customerId));

        Account.AccountType accountType = Account.AccountType.valueOf(accountTypeStr);
        Account.Status status = Account.Status.valueOf(statusStr);

        return new Account(accountId, customer, accountType, balance, status, createdAt);
    }


    @Override
    public void addAccount(Account account) throws SQLException {

        String sql = "INSERT INTO accounts (customer_id, account_type, balance, status, created_at) VALUES (?,?,?,?,?)";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, account.getCustomer().getCustomerId());
            preparedStatement.setString(2, account.getAccountType().name());
            preparedStatement.setBigDecimal(3, account.getBalance());
            preparedStatement.setString(4, account.getStatus().name());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(account.getCreatedAt()));

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        account.setAccountId(resultSet.getLong(1));
                    } else {
                        throw new SQLException("Creating account failed");
                    }
                }
            }
            else {
                throw new SQLException("Creating account failed");
            }

        }

    }

    @Override
    public void addAccount(Account account, Connection connection) throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance, status, created_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1,account.getCustomer().getCustomerId());
            preparedStatement.setString(2,account.getAccountType().name());
            preparedStatement.setBigDecimal(3, account.getBalance());
            preparedStatement.setString(4, account.getStatus().name());
            preparedStatement.setTimestamp(5,Timestamp.valueOf(account.getCreatedAt()));

            int rowsAffected = preparedStatement.executeUpdate();

            if(rowsAffected > 0)
            {
                try(ResultSet resultSet = preparedStatement.getGeneratedKeys())
                {
                    if(resultSet.next())
                    {
                        account.setAccountId(resultSet.getLong(1));
                    }
                    else {
                        throw new SQLException("Creating account failed");
                    }
                }
            }
            else {
                throw new SQLException("Creating the account failed");
            }

        }

    }

    @Override
    public Optional<Account> getAccountById(long accountId) throws SQLException {
        return Optional.empty();
    }

    @Override
    public Optional<Account> getAccountById(long accountId, Connection connection) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<Account> getAccountsByCustomerId(long customerId) throws SQLException {
        return List.of();
    }

    @Override
    public List<Account> getAccountsByCustomerId(long customerId, Connection connection) throws SQLException {
        return List.of();
    }

    @Override
    public void updateBalance(long accountId, double newBalance) throws SQLException {

    }

    @Override
    public void updateBalance(long accountId, double newBalance, Connection connection) throws SQLException {

    }

    @Override
    public void deleteAccount(long accountId) throws SQLException {

    }

    @Override
    public void deleteAccount(long accountId, Connection connection) throws SQLException {

    }
}
