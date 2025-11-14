package com.corebank.dao;

import com.corebank.model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TransactionDAO {

    long createTransaction(Transaction transaction) throws SQLException;
    long createTransaction(Transaction transaction, Connection connection) throws SQLException;

    Optional<Transaction> getTransactionById(long transactionId) throws SQLException;
    Optional<Transaction> getTransactionById(long transactionId, Connection connection) throws SQLException;

    List<Transaction> getTransactionsByAccountId(long accountId) throws SQLException;
    List<Transaction> getTransactionsByAccountId(long accountId, Connection connection) throws SQLException;

    void updateTransaction(Transaction transaction) throws SQLException;
    void updateTransaction(Transaction transaction, Connection connection) throws SQLException;

    void deleteTransaction(long transactionId) throws SQLException;
    void deleteTransaction(long transactionId, Connection connection) throws SQLException;
}

