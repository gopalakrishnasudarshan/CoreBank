package com.corebank.dao;

import com.corebank.model.Transfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TransferDAO {
    long createTransfer(Transfer transfer) throws SQLException;
    long createTransfer(Transfer transfer, Connection connection) throws SQLException;

    Optional<Transfer> getTransferById(long transferId) throws SQLException;
    Optional<Transfer> getTransferById(long transferId, Connection connection) throws SQLException;

    List<Transfer> getTransfersByAccountId(long accountId) throws SQLException;
    List<Transfer> getTransfersByAccountId(long accountId, Connection connection) throws SQLException;

    void updateTransfer(Transfer transfer) throws SQLException;
    void updateTransfer(Transfer transfer, Connection connection) throws SQLException;

    void deleteTransfer(long transferId) throws SQLException;
    void deleteTransfer(long transferId, Connection connection) throws SQLException;
}
