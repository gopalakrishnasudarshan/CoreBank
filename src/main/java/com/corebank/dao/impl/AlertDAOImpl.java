package com.corebank.dao.impl;

import com.corebank.dao.AlertDAO;
import com.corebank.dao.AccountDAO;
import com.corebank.dao.UserDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.exception.DataAccessException;
import com.corebank.model.Alert;
import com.corebank.model.Account;
import com.corebank.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlertDAOImpl implements AlertDAO {

    private AccountDAO accountDAO = new AccountDAOImpl();
    private UserDAO userDAO = new UserDAOImpl();
    private Logger logger = LoggerFactory.getLogger(AlertDAOImpl.class);

    private Alert mapRowToAlert(ResultSet rs) {
        try {
            long alertId = rs.getLong("alert_id");
            long accountId = rs.getLong("account_id");
            Long userId = rs.getObject("user_id") != null ? rs.getLong("user_id") : null;
            String type = rs.getString("type");
            String message = rs.getString("message");
            String statusStr = rs.getString("status");
            Timestamp ts = rs.getTimestamp("created_at");

            Account account = accountDAO.getAccountById(accountId)
                    .orElseThrow(() -> new DataAccessException("Account not found for id: " + accountId));

            User user = null;
            if (userId != null) {
                user = userDAO.getUser(userId)
                        .orElse(null);
            }

            Alert.Status status = statusStr != null
                    ? Alert.Status.valueOf(statusStr.trim().toUpperCase())
                    : Alert.Status.PENDING;

            return new Alert(alertId, account, user, type, message, status, ts != null ? ts.toLocalDateTime() : null);

        } catch (SQLException e) {
            throw new DataAccessException("Error mapping alert from ResultSet", e);
        }
    }

    @Override
    public long createAlert(Alert alert) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return createAlert(alert, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error creating alert", e);
        }
    }

    @Override
    public long createAlert(Alert alert, Connection connection) {
        String sql = "INSERT INTO alerts(account_id, user_id, type, message, status, created_at) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, alert.getAccount().getAccountId());
            if (alert.getUser() != null) ps.setLong(2, alert.getUser().getUserId());
            else ps.setNull(2, Types.BIGINT);
            ps.setString(3, alert.getType());
            ps.setString(4, alert.getMessage());
            ps.setString(5, alert.getStatus() != null ? alert.getStatus().name() : Alert.Status.PENDING.name());
            ps.setTimestamp(6, alert.getCreatedAt() != null ? Timestamp.valueOf(alert.getCreatedAt()) : null);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        alert.setAlertId(generatedId);
                        logger.info("Alert created successfully with id: {}", generatedId);
                        return generatedId;
                    } else {
                        throw new DataAccessException("Creating alert failed, no ID returned");
                    }
                }
            } else {
                throw new DataAccessException("Creating alert failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating alert", e);
        }
    }

    @Override
    public Optional<Alert> getAlertById(long alertId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getAlertById(alertId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching alert with id " + alertId, e);
        }
    }

    @Override
    public Optional<Alert> getAlertById(long alertId, Connection connection) {
        String sql = "SELECT * FROM alerts WHERE alert_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, alertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRowToAlert(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching alert with id " + alertId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Alert> getAlertsByAccountId(long accountId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getAlertsByAccountId(accountId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching alerts for account id " + accountId, e);
        }
    }

    @Override
    public List<Alert> getAlertsByAccountId(long accountId, Connection connection) {
        String sql = "SELECT * FROM alerts WHERE account_id = ?";
        List<Alert> alerts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapRowToAlert(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching alerts for account id " + accountId, e);
        }
        return alerts;
    }

    @Override
    public List<Alert> getAlertsByUserId(long userId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getAlertsByUserId(userId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching alerts for user id " + userId, e);
        }
    }

    @Override
    public List<Alert> getAlertsByUserId(long userId, Connection connection) {
        String sql = "SELECT * FROM alerts WHERE user_id = ?";
        List<Alert> alerts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) alerts.add(mapRowToAlert(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching alerts for user id " + userId, e);
        }
        return alerts;
    }

    @Override
    public List<Alert> getAlertsByStatus(Alert.Status status) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getAlertsByStatus(status, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching alerts with status " + status, e);
        }
    }

    @Override
    public List<Alert> getAlertsByStatus(Alert.Status status, Connection connection) {
        String sql = "SELECT * FROM alerts WHERE status = ?";
        List<Alert> alerts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) alerts.add(mapRowToAlert(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching alerts with status " + status, e);
        }
        return alerts;
    }

    @Override
    public void updateAlertStatus(long alertId, Alert.Status newStatus) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            updateAlertStatus(alertId, newStatus, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating alert status for id " + alertId, e);
        }
    }

    @Override
    public void updateAlertStatus(long alertId, Alert.Status newStatus, Connection connection) {
        String sql = "UPDATE alerts SET status = ? WHERE alert_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setLong(2, alertId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Updating alert status failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating alert status for id " + alertId, e);
        }
    }

    @Override
    public void deleteAlert(long alertId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            deleteAlert(alertId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting alert with id " + alertId, e);
        }
    }

    @Override
    public void deleteAlert(long alertId, Connection connection) {
        String sql = "DELETE FROM alerts WHERE alert_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, alertId);
            int rows = ps.executeUpdate();
            if (rows > 0) logger.info("Alert with id {} deleted successfully", alertId);
            else logger.warn("No alert found with id {}", alertId);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting alert with id " + alertId, e);
        }
    }

    @Override
    public List<Alert> getAllAlerts() {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getAllAlerts(connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all alerts", e);
        }
    }

    @Override
    public List<Alert> getAllAlerts(Connection connection) {
        String sql = "SELECT * FROM alerts";
        List<Alert> alerts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) alerts.add(mapRowToAlert(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all alerts", e);
        }
        return alerts;
    }
}
