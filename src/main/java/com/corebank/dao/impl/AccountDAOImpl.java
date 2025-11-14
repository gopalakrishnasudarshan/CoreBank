package com.corebank.dao.impl;

import com.corebank.dao.AccountDAO;
import com.corebank.dao.CustomerDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.exception.DataAccessException;
import com.corebank.model.Account;
import com.corebank.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAOImpl implements AccountDAO {


    CustomerDAO customerDAO = new CustomerDAOImpl();
    Logger logger = LoggerFactory.getLogger(AccountDAOImpl.class);

    //helper function , because in the Account model we have A customer Object.
    private Account mapRowToAccount(ResultSet resultSet) {
        try {
            long accountId = resultSet.getLong("account_id");
            long customerId = resultSet.getLong("customer_id");
            String accountTypeStr = resultSet.getString("account_type");
            BigDecimal balance = resultSet.getBigDecimal("balance");
            String statusStr = resultSet.getString("status");
            Timestamp ts = resultSet.getTimestamp("created_at");
            LocalDateTime createdAt = (ts != null) ? ts.toLocalDateTime() : null;

            Customer customer = customerDAO.getCustomerById(customerId)
                    .orElseThrow(() -> new DataAccessException("Customer not found for id: " + customerId));

            Account.AccountType accountType = Account.AccountType.valueOf(accountTypeStr.trim().toUpperCase());
            Account.Status status = Account.Status.valueOf(statusStr.trim().toUpperCase());

            return new Account(accountId, customer, accountType, balance, status, createdAt);

        } catch (SQLException e) {
            throw new DataAccessException("Error mapping account from ResultSet", e);
        } catch (IllegalArgumentException e) {
            throw new DataAccessException("Invalid enum value in account table", e);
        }
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
            } else {
                throw new SQLException("Creating account failed");
            }

        }

    }

    @Override
    public void addAccount(Account account, Connection connection) throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance, status, created_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
            } else {
                throw new SQLException("Creating the account failed");
            }

        }

    }

    @Override
    public Optional<Account> getAccountById(long accountId) {
        if (accountId <= 0) {
            return Optional.empty();
        }

        String sql = "SELECT account_id, customer_id, account_type, balance, status, created_at FROM accounts WHERE account_id = ?";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, accountId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Account account = mapRowToAccount(resultSet);
                    return Optional.of(account);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error fetching account with accountId " + accountId, e);
        }

        return Optional.empty();
    }


    @Override
    public Optional<Account> getAccountById(long accountId, Connection connection) throws SQLException {
        String sql = "SELECT account_id, customer_id, account_type, balance, status, created_at FROM accounts where account_id =?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
            preparedStatement.setLong(1, accountId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Account account = mapRowToAccount(resultSet);

                    return Optional.of(account);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Account> getAccountsByCustomerId(long customerId) throws SQLException {

        String sql = "SELECT account_id, customer_id, account_type, balance, status, created_at FROM accounts where customer_id=? ";

        List<Account> accounts = new ArrayList<>();
        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, customerId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Account account = mapRowToAccount(resultSet);
                    accounts.add(account);

                }

            }

        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsByCustomerId(long customerId, Connection connection) {
        String sql = "SELECT account_id, customer_id, account_type, balance, status, created_at FROM accounts WHERE customer_id = ?";
        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, customerId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Account account = mapRowToAccount(resultSet);
                    accounts.add(account);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error fetching accounts for customer_id " + customerId, e);
        }

        return accounts;
    }


    @Override
    public void updateBalance(long accountId, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setBigDecimal(1, newBalance);
            preparedStatement.setLong(2, accountId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Updated balance successfully for account_id {}", accountId);
            } else {
                logger.warn("No account found with account_id {}", accountId);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error updating balance for account_id " + accountId, e);
        }
    }

    @Override
    public void updateBalance(long accountId, BigDecimal newBalance, Connection connection) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setBigDecimal(1, newBalance);
            preparedStatement.setLong(2, accountId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Updated balance successfully for account_id {}", accountId);
            } else {
                logger.warn("No account found with account_id {}", accountId);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error updating balance for account_id " + accountId, e);
        }
    }


    @Override
    public void deleteAccount(long accountId) {
        if (accountId <= 0) {
            logger.warn("Invalid account id: {}", accountId);
            return;
        }

        String sql = "DELETE FROM accounts WHERE account_id = ?";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, accountId);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Account with account_id {} deleted successfully", accountId);
            } else {
                logger.warn("No account found with account_id: {}", accountId);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting account with account_id " + accountId, e);
        }
    }

    @Override
    public void deleteAccount(long accountId, Connection connection) {
        if (accountId <= 0) {
            logger.warn("Invalid account id: {}", accountId);
            return;
        }

        String sql = "DELETE FROM accounts WHERE account_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, accountId);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Account with account_id {} deleted successfully", accountId);
            } else {
                logger.warn("No account found with account_id: {}", accountId);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting account with account_id " + accountId, e);
        }
    }

}
