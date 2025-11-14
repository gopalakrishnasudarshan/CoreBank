package com.corebank.dao;

import com.corebank.model.Customer;
import com.corebank.model.Loan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LoanDAO {

    void addLoan(Loan loan) throws SQLException;
    void addLoan(Loan loan, Connection connection) throws SQLException;

    Optional<Loan> getLoanById(long loanId) throws SQLException;
    Optional<Loan> getLoanById(long loanId, Connection connection) throws SQLException;

    List<Loan> getLoansByCustomer(Customer customer) throws SQLException;
    List<Loan> getLoansByCustomer(long customerId) throws SQLException;
    List<Loan> getLoansByCustomer(long customerId, Connection connection) throws SQLException;

    List<Loan> getAllLoans() throws SQLException;
    List<Loan> getAllLoans(Connection connection) throws SQLException;

    List<Loan> getLoansByStatus(Loan.Status status) throws SQLException;
    List<Loan> getLoansByStatus(Loan.Status status, Connection connection) throws SQLException;

    void updateLoan(Loan loan) throws SQLException;
    void updateLoan(Loan loan, Connection connection) throws SQLException;

    void approveLoan(long loanId) throws SQLException;
    void approveLoan(Loan loan) throws SQLException;
    void approveLoan(long loanId, Connection connection) throws SQLException;
    void approveLoan(Loan loan, Connection connection) throws SQLException;

    void rejectLoan(long loanId) throws SQLException;
    void rejectLoan(Loan loan) throws SQLException;
    void rejectLoan(long loanId, Connection connection) throws SQLException;
    void rejectLoan(Loan loan, Connection connection) throws SQLException;

    void deleteLoan(long loanId) throws SQLException;
    void deleteLoan(Loan loan) throws SQLException;
    void deleteLoan(long loanId, Connection connection) throws SQLException;
    void deleteLoan(Loan loan, Connection connection) throws SQLException;
}
