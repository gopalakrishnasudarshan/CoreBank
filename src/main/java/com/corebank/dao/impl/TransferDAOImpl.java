package com.corebank.dao.impl;

import com.corebank.dao.AccountDAO;
import com.corebank.dao.TransferDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.exception.DataAccessException;
import com.corebank.model.Account;
import com.corebank.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferDAOImpl implements TransferDAO {

    private AccountDAO accountDAO = new AccountDAOImpl();
    private Logger logger = LoggerFactory.getLogger(TransferDAOImpl.class);

    // Helper method to map ResultSet to Transfer
    private Transfer mapRowToTransfer(ResultSet resultSet) {
        try {
            long transferId = resultSet.getLong("transfer_id");
            long fromAccountId = resultSet.getLong("from_account_id");
            long toAccountId = resultSet.getLong("to_account_id");
            BigDecimal amount = resultSet.getBigDecimal("amount");
            Timestamp timestamp = resultSet.getTimestamp("timestamp");

            Account fromAccount = accountDAO.getAccountById(fromAccountId)
                    .orElseThrow(() -> new DataAccessException("From account not found: " + fromAccountId));

            Account toAccount = accountDAO.getAccountById(toAccountId)
                    .orElseThrow(() -> new DataAccessException("To account not found: " + toAccountId));

            LocalDateTime dateTime = (timestamp != null) ? timestamp.toLocalDateTime() : null;

            return new Transfer(transferId, fromAccount, toAccount, amount, dateTime);
        } catch (SQLException e) {
            throw new DataAccessException("Error mapping transfer from ResultSet", e);
        }
    }

    @Override
    public long createTransfer(Transfer transfer) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return createTransfer(transfer, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error creating transfer", e);
        }
    }

    @Override
    public long createTransfer(Transfer transfer, Connection connection) {
        String sql = "INSERT INTO transfers(from_account_id, to_account_id, amount, timestamp) VALUES (?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, transfer.getFromAccount().getAccountId());
            preparedStatement.setLong(2, transfer.getToAccount().getAccountId());
            preparedStatement.setBigDecimal(3, transfer.getAmount());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(transfer.getTimestamp()));

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long generatedId = generatedKeys.getLong(1);
                        transfer.setTransferId(generatedId);
                        logger.info("Transfer created successfully with id: {}", generatedId);
                        return generatedId;
                    } else {
                        throw new DataAccessException("Creating transfer failed, no ID returned");
                    }
                }
            } else {
                throw new DataAccessException("Creating transfer failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating transfer", e);
        }
    }

    @Override
    public Optional<Transfer> getTransferById(long transferId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getTransferById(transferId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching transfer with id " + transferId, e);
        }
    }

    @Override
    public Optional<Transfer> getTransferById(long transferId, Connection connection) {
        String sql = "SELECT * FROM transfers WHERE transfer_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, transferId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToTransfer(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching transfer with id " + transferId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Transfer> getTransfersByAccountId(long accountId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getTransfersByAccountId(accountId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching transfers for account id " + accountId, e);
        }
    }

    @Override
    public List<Transfer> getTransfersByAccountId(long accountId, Connection connection) {
        String sql = "SELECT * FROM transfers WHERE from_account_id = ? OR to_account_id = ?";
        List<Transfer> transfers = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, accountId);
            preparedStatement.setLong(2, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    transfers.add(mapRowToTransfer(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching transfers for account id " + accountId, e);
        }
        return transfers;
    }

    @Override
    public void updateTransfer(Transfer transfer) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            updateTransfer(transfer, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating transfer with id " + transfer.getTransferId(), e);
        }
    }

    @Override
    public void updateTransfer(Transfer transfer, Connection connection) {
        String sql = "UPDATE transfers SET from_account_id = ?, to_account_id = ?, amount = ?, timestamp = ? WHERE transfer_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, transfer.getFromAccount().getAccountId());
            preparedStatement.setLong(2, transfer.getToAccount().getAccountId());
            preparedStatement.setBigDecimal(3, transfer.getAmount());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(transfer.getTimestamp()));
            preparedStatement.setLong(5, transfer.getTransferId());

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Updating transfer failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating transfer with id " + transfer.getTransferId(), e);
        }
    }

    @Override
    public void deleteTransfer(long transferId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            deleteTransfer(transferId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting transfer with id " + transferId, e);
        }
    }

    @Override
    public void deleteTransfer(long transferId, Connection connection) {
        String sql = "DELETE FROM transfers WHERE transfer_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, transferId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Transfer with id {} deleted successfully", transferId);
            } else {
                logger.warn("No transfer found with id {}", transferId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting transfer with id " + transferId, e);
        }
    }
}
