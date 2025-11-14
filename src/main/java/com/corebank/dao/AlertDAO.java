package com.corebank.dao;

import com.corebank.model.Alert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AlertDAO {
    long createAlert(Alert alert) throws SQLException;
    long createAlert(Alert alert, Connection connection) throws SQLException;

    Optional<Alert> getAlertById(long alertId) throws SQLException;
    Optional<Alert> getAlertById(long alertId, Connection connection) throws SQLException;

    List<Alert> getAlertsByAccountId(long accountId) throws SQLException;
    List<Alert> getAlertsByAccountId(long accountId, Connection connection) throws SQLException;

    List<Alert> getAlertsByUserId(long userId) throws SQLException;
    List<Alert> getAlertsByUserId(long userId, Connection connection) throws SQLException;

    List<Alert> getAlertsByStatus(Alert.Status status) throws SQLException;
    List<Alert> getAlertsByStatus(Alert.Status status, Connection connection) throws SQLException;

    void updateAlertStatus(long alertId, Alert.Status newStatus) throws SQLException;
    void updateAlertStatus(long alertId, Alert.Status newStatus, Connection connection) throws SQLException;

    void deleteAlert(long alertId) throws SQLException;
    void deleteAlert(long alertId, Connection connection) throws SQLException;

    List<Alert> getAllAlerts() throws SQLException;
    List<Alert> getAllAlerts(Connection connection) throws SQLException;
}
