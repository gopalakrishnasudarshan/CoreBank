package com.corebank.dao;

import com.corebank.model.Customer;
import com.corebank.model.Loan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LoanDAO {

    void addLoan(Loan loan) throws SQLException;

    // Add a new loan using an existing connection (for transactions)
    void addLoan(Loan loan, Connection connection) throws SQLException;


    // ----------------------
    // READ
    // ----------------------

    // Get a loan by its ID (own connection)
    Optional<Loan> getLoanById(long loanId) throws SQLException;

    // Get a loan by its ID using existing connection
    Optional<Loan> getLoanById(long loanId, Connection connection) throws SQLException;

    // Get all loans of a customer (by Customer object)
    List<Loan> getLoansByCustomer(Customer customer) throws SQLException;

    // Get all loans of a customer using customerId (own connection)
    List<Loan> getLoansByCustomer(long customerId) throws SQLException;

    // Get all loans of a customer using existing connection
    List<Loan> getLoansByCustomer(long customerId, Connection connection) throws SQLException;

    // Get all loans in the system
    List<Loan> getAllLoans() throws SQLException;

    // Get all loans using existing connection
    List<Loan> getAllLoans(Connection connection) throws SQLException;

    // Get loans by status (PENDING, APPROVED, etc.)
    List<Loan> getLoansByStatus(Loan.Status status) throws SQLException;

    // Get loans by status using existing connection
    List<Loan> getLoansByStatus(Loan.Status status, Connection connection) throws SQLException;


    // ----------------------
    // UPDATE
    // ----------------------

    // Update an existing loan (status, dates, amount, interestRate, etc.)
    void updateLoan(Loan loan) throws SQLException;

    // Update an existing loan using existing connection
    void updateLoan(Loan loan, Connection connection) throws SQLException;

    // Approve a loan by ID
    void approveLoan(long loanId) throws SQLException;

    // Approve a loan by Loan object
    void approveLoan(Loan loan) throws SQLException;

    // Approve a loan using existing connection
    void approveLoan(long loanId, Connection connection) throws SQLException;
    void approveLoan(Loan loan, Connection connection) throws SQLException;

    // Reject a loan by ID
    void rejectLoan(long loanId) throws SQLException;

    // Reject a loan by Loan object
    void rejectLoan(Loan loan) throws SQLException;

    // Reject a loan using existing connection
    void rejectLoan(long loanId, Connection connection) throws SQLException;
    void rejectLoan(Loan loan, Connection connection) throws SQLException;


    // ----------------------
    // DELETE
    // ----------------------

    // Delete a loan by ID
    void deleteLoan(long loanId) throws SQLException;

    // Delete a loan by Loan object
    void deleteLoan(Loan loan) throws SQLException;

    // Delete a loan using existing connection
    void deleteLoan(long loanId, Connection connection) throws SQLException;
    void deleteLoan(Loan loan, Connection connection) throws SQLException;
}
