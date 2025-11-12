package com.corebank.dao.impl;

import com.corebank.dao.CustomerDAO;
import com.corebank.dao.LoanDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.model.Customer;
import com.corebank.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class LoanDAOImpl implements LoanDAO {

    private CustomerDAO customerDAO = new CustomerDAOImpl();
    private Logger logger = LoggerFactory.getLogger(LoanDAOImpl.class);

    //helper function

    private Loan mapRowToLoan(ResultSet resultSet) throws SQLException {
        long loanId = resultSet.getLong("loan_id");
        long customerId = resultSet.getLong("customer_id");
        BigDecimal amount = resultSet.getBigDecimal("amount");
        BigDecimal interestRate = resultSet.getBigDecimal("interest_rate");
        Date startDateSql = resultSet.getDate("start_date");
        Date endDateSql = resultSet.getDate("end_date");
        String statusStr = resultSet.getString("status");

        Customer customer = customerDAO.getCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found for id: " + customerId));

        LocalDate startDate = (startDateSql != null) ? ((java.sql.Date) startDateSql).toLocalDate() : null;
        LocalDate endDate = (endDateSql != null) ? ((java.sql.Date) endDateSql).toLocalDate() : null;
        Loan.Status status = (statusStr != null) ? Loan.Status.valueOf(statusStr) : Loan.Status.PENDING;

        return new Loan(loanId, customer, amount, interestRate, startDate, endDate, status);

    }

    @Override
    public void addLoan(Loan loan) throws SQLException {

        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            addLoan(loan, connection);
        }

    }

    @Override
    public void addLoan(Loan loan, Connection connection) throws SQLException {

        String sql = "INSERT INTO loans (customer_id, amount, interest_rate, start_date, end_date, status) " +
                "VALUES (?,?,?,?,?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, loan.getCustomer().getCustomerId());
            preparedStatement.setBigDecimal(2, loan.getAmount());
            preparedStatement.setBigDecimal(3, loan.getInterestRate());
            preparedStatement.setDate(4, java.sql.Date.valueOf(loan.getStartDate()));
            preparedStatement.setDate(5, java.sql.Date.valueOf(loan.getEndDate()));
            preparedStatement.setString(6, loan.getStatus() != null ? loan.getStatus().name() : Loan.Status.PENDING.name());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        loan.setLoanId(resultSet.getLong(1));
                        logger.info("Loan created successfully with id: {}", loan.getLoanId());
                    } else {
                        throw new SQLException("Creating loan faild");
                    }
                }
            } else {
                throw new SQLException("Creating loan failed");
            }
        }

    }

    @Override
    public Optional<Loan> getLoanById(long loanId) throws SQLException {


        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getLoanById(loanId, connection);
        }


    }

    @Override
    public Optional<Loan> getLoanById(long loanId, Connection connection) throws SQLException {

        String sql = "SELECT * FROM loans WHERE loan_id = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, loanId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    Loan loan = mapRowToLoan(resultSet);
                    return Optional.of(loan);
                }
            }

        }


        return Optional.empty();
    }

    @Override
    public List<Loan> getLoansByCustomer(Customer customer) throws SQLException {

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        return getLoansByCustomer(customer.getCustomerId());
    }

    @Override
    public List<Loan> getLoansByCustomer(long customerId) throws SQLException {

        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getLoansByCustomer(customerId, connection);
        }

    }

    @Override
    public List<Loan> getLoansByCustomer(long customerId, Connection connection) throws SQLException {

        String sql = "SELECT * FROM loans WHERE customer_id = ? ";
        List<Loan> loans = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, customerId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Loan loan = mapRowToLoan(resultSet);
                    loans.add(loan);
                }
            }
        }

        return loans;
    }


    @Override
    public List<Loan> getAllLoans() throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getAllLoans(connection);
        }
    }

    @Override
    public List<Loan> getAllLoans(Connection connection) throws SQLException {
        String sql = "SELECT * FROM loans";
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                loans.add(mapRowToLoan(resultSet));
            }
        }
        return loans;
    }

    @Override
    public List<Loan> getLoansByStatus(Loan.Status status) throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getLoansByStatus(status, connection);
        }
    }

    @Override
    public List<Loan> getLoansByStatus(Loan.Status status, Connection connection) throws SQLException {
        String sql = "SELECT * FROM loans WHERE status = ?";
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, status.name());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(mapRowToLoan(resultSet));
                }
            }
        }
        return loans;
    }

    @Override
    public void updateLoan(Loan loan) throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            updateLoan(loan, connection);
        }
    }

    @Override
    public void updateLoan(Loan loan, Connection connection) throws SQLException {
        String sql = "UPDATE loans SET customer_id = ?, amount = ?, interest_rate = ?, start_date = ?, end_date = ?, status = ? " +
                "WHERE loan_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, loan.getCustomer().getCustomerId());
            preparedStatement.setBigDecimal(2, loan.getAmount());
            preparedStatement.setBigDecimal(3, loan.getInterestRate());
            preparedStatement.setDate(4, java.sql.Date.valueOf(loan.getStartDate()));
            preparedStatement.setDate(5, java.sql.Date.valueOf(loan.getEndDate()));
            preparedStatement.setString(6, loan.getStatus().name());
            preparedStatement.setLong(7, loan.getLoanId());

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Updating loan failed, no rows affected.");
            }
        }
    }

    // Approve loan

    @Override
    public void approveLoan(long loanId) throws SQLException {
        Optional<Loan> optionalLoan = getLoanById(loanId);
        if (optionalLoan.isPresent()) {
            approveLoan(optionalLoan.get()); // you already throw SQLException here
        }
    }
    @Override
    public void approveLoan(Loan loan) throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            approveLoan(loan, connection);
        }
    }

    @Override
    public void approveLoan(long loanId, Connection connection) throws SQLException {
        getLoanById(loanId, connection).ifPresent(loan -> {
            try {
                approveLoan(loan, connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void approveLoan(Loan loan, Connection connection) throws SQLException {
        loan.setStatus(Loan.Status.APPROVED);
        updateLoan(loan, connection);
    }

    // Reject loan
    @Override
    public void rejectLoan(long loanId) throws SQLException {
        Optional<Loan> optionalLoan = getLoanById(loanId);
        if (optionalLoan.isPresent()) {
            rejectLoan(optionalLoan.get());
        }
    }

    @Override
    public void rejectLoan(Loan loan) throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            rejectLoan(loan, connection);
        }
    }

    @Override
    public void rejectLoan(long loanId, Connection connection) throws SQLException {
        getLoanById(loanId, connection).ifPresent(loan -> {
            try {
                rejectLoan(loan, connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void rejectLoan(Loan loan, Connection connection) throws SQLException {
        loan.setStatus(Loan.Status.REJECTED);
        updateLoan(loan, connection);
    }

    // Delete loan
    @Override
    public void deleteLoan(long loanId) throws SQLException {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            deleteLoan(loanId, connection);
        }
    }

    @Override
    public void deleteLoan(Loan loan) throws SQLException {
        deleteLoan(loan.getLoanId());
    }

    @Override
    public void deleteLoan(long loanId, Connection connection) throws SQLException {
        String sql = "DELETE FROM loans WHERE loan_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, loanId);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void deleteLoan(Loan loan, Connection connection) throws SQLException {
        deleteLoan(loan.getLoanId(), connection);
    }
}
