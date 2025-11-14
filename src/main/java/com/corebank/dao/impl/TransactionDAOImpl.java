package com.corebank.dao.impl;

import com.corebank.dao.TransactionDAO;
import com.corebank.dao.AccountDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.exception.DataAccessException;
import com.corebank.model.Account;
import com.corebank.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDAOImpl implements TransactionDAO {

    private AccountDAO accountDAO = new AccountDAOImpl();
    private Logger logger = LoggerFactory.getLogger(TransactionDAOImpl.class);

    // Helper: map ResultSet row to Transaction object
    private Transaction mapRowToTransaction(ResultSet resultSet) {
        try {
            long transactionId = resultSet.getLong("transaction_id");
            long accountId = resultSet.getLong("account_id");
            String typeStr = resultSet.getString("type");
            BigDecimal amount = resultSet.getBigDecimal("amount");
            Timestamp timestampSql = resultSet.getTimestamp("timestamp");

            Account account = accountDAO.getAccountById(accountId)
                    .orElseThrow(() -> new DataAccessException("Account not found for id: " + accountId));

            Transaction.Type type = Transaction.Type.valueOf(typeStr.trim().toUpperCase());
            return new Transaction(transactionId, account, type, amount, timestampSql.toLocalDateTime());
        } catch (SQLException e) {
            throw new DataAccessException("Error mapping transaction from ResultSet", e);
        } catch (IllegalArgumentException e) {
            throw new DataAccessException("Invalid enum value in transaction table", e);
        }
    }

    // CREATE
    @Override
    public long createTransaction(Transaction transaction) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return createTransaction(transaction, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error creating transaction", e);
        }
    }

    @Override
    public long createTransaction(Transaction transaction, Connection connection) {
        String sql = "INSERT INTO transactions(account_id, type, amount, timestamp) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, transaction.getAccount().getAccountId());
            ps.setString(2, transaction.getType().name());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        transaction.setTransactionId(id);
                        logger.info("Transaction created successfully with id: {}", id);
                        return id;
                    } else {
                        throw new DataAccessException("Creating transaction failed, no ID returned");
                    }
                }
            } else {
                throw new DataAccessException("Creating transaction failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating transaction", e);
        }
    }

    // READ
    @Override
    public Optional<Transaction> getTransactionById(long transactionId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getTransactionById(transactionId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching transaction with id " + transactionId, e);
        }
    }

    @Override
    public Optional<Transaction> getTransactionById(long transactionId, Connection connection) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching transaction with id " + transactionId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(long accountId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getTransactionsByAccountId(accountId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching transactions for account_id " + accountId, e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(long accountId, Connection connection) {
        String sql = "SELECT * FROM transactions WHERE account_id = ?";
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRowToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching transactions for account_id " + accountId, e);
        }
        return transactions;
    }

    // UPDATE
    @Override
    public void updateTransaction(Transaction transaction) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            updateTransaction(transaction, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating transaction with id " + transaction.getTransactionId(), e);
        }
    }

    @Override
    public void updateTransaction(Transaction transaction, Connection connection) {
        String sql = "UPDATE transactions SET account_id = ?, type = ?, amount = ?, timestamp = ? WHERE transaction_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, transaction.getAccount().getAccountId());
            ps.setString(2, transaction.getType().name());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));
            ps.setLong(5, transaction.getTransactionId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Updating transaction failed, no rows affected");
            }
            logger.info("Transaction with id {} updated successfully", transaction.getTransactionId());
        } catch (SQLException e) {
            throw new DataAccessException("Error updating transaction with id " + transaction.getTransactionId(), e);
        }
    }

    // DELETE
    @Override
    public void deleteTransaction(long transactionId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            deleteTransaction(transactionId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting transaction with id " + transactionId, e);
        }
    }

    @Override
    public void deleteTransaction(long transactionId, Connection connection) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, transactionId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("Transaction with id {} deleted successfully", transactionId);
            } else {
                logger.warn("No transaction found with id {}", transactionId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting transaction with id " + transactionId, e);
        }
    }
}
