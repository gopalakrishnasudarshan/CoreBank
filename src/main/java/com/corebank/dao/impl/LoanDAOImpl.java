package com.corebank.dao.impl;

import com.corebank.dao.CustomerDAO;
import com.corebank.dao.LoanDAO;
import com.corebank.db.DBConnectionManager;
import com.corebank.exception.DataAccessException;
import com.corebank.model.Customer;
import com.corebank.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAOImpl implements LoanDAO {

    private CustomerDAO customerDAO = new CustomerDAOImpl();
    private Logger logger = LoggerFactory.getLogger(LoanDAOImpl.class);

    // Helper function
    private Loan mapRowToLoan(ResultSet resultSet) {
        try {
            long loanId = resultSet.getLong("loan_id");
            long customerId = resultSet.getLong("customer_id");
            BigDecimal amount = resultSet.getBigDecimal("amount");
            BigDecimal interestRate = resultSet.getBigDecimal("interest_rate");
            Date startDateSql = resultSet.getDate("start_date");
            Date endDateSql = resultSet.getDate("end_date");
            String statusStr = resultSet.getString("status");

            Customer customer = customerDAO.getCustomerById(customerId)
                    .orElseThrow(() -> new DataAccessException("Customer not found for id: " + customerId));

            LocalDate startDate = (startDateSql != null) ? ((java.sql.Date) startDateSql).toLocalDate() : null;
            LocalDate endDate = (endDateSql != null) ? ((java.sql.Date) endDateSql).toLocalDate() : null;

            Loan.Status status = (statusStr != null)
                    ? Loan.Status.valueOf(statusStr.trim().toUpperCase())
                    : Loan.Status.PENDING;

            return new Loan(loanId, customer, amount, interestRate, startDate, endDate, status);

        } catch (SQLException e) {
            throw new DataAccessException("Error mapping loan from ResultSet", e);
        } catch (IllegalArgumentException e) {
            throw new DataAccessException("Invalid enum value in loan table", e);
        }
    }

    @Override
    public void addLoan(Loan loan) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            addLoan(loan, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error adding loan", e);
        }
    }

    @Override
    public void addLoan(Loan loan, Connection connection) {
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
                        throw new DataAccessException("Creating loan failed, no ID returned");
                    }
                }
            } else {
                throw new DataAccessException("Creating loan failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error adding loan", e);
        }
    }

    @Override
    public Optional<Loan> getLoanById(long loanId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getLoanById(loanId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching loan with id " + loanId, e);
        }
    }

    @Override
    public Optional<Loan> getLoanById(long loanId, Connection connection) {
        String sql = "SELECT * FROM loans WHERE loan_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, loanId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToLoan(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching loan with id " + loanId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Loan> getLoansByCustomer(Customer customer) {
        if (customer == null) throw new IllegalArgumentException("Customer cannot be null");
        return getLoansByCustomer(customer.getCustomerId());
    }

    @Override
    public List<Loan> getLoansByCustomer(long customerId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getLoansByCustomer(customerId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching loans for customer_id " + customerId, e);
        }
    }

    @Override
    public List<Loan> getLoansByCustomer(long customerId, Connection connection) {
        String sql = "SELECT * FROM loans WHERE customer_id = ?";
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, customerId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(mapRowToLoan(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching loans for customer_id " + customerId, e);
        }
        return loans;
    }

    @Override
    public List<Loan> getAllLoans() {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getAllLoans(connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all loans", e);
        }
    }

    @Override
    public List<Loan> getAllLoans(Connection connection) {
        String sql = "SELECT * FROM loans";
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                loans.add(mapRowToLoan(resultSet));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching all loans", e);
        }
        return loans;
    }

    @Override
    public List<Loan> getLoansByStatus(Loan.Status status) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            return getLoansByStatus(status, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching loans by status " + status, e);
        }
    }

    @Override
    public List<Loan> getLoansByStatus(Loan.Status status, Connection connection) {
        String sql = "SELECT * FROM loans WHERE status = ?";
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, status.name());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(mapRowToLoan(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching loans by status " + status, e);
        }
        return loans;
    }

    @Override
    public void updateLoan(Loan loan) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            updateLoan(loan, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating loan with id " + loan.getLoanId(), e);
        }
    }

    @Override
    public void updateLoan(Loan loan, Connection connection) {
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
                throw new DataAccessException("Updating loan failed, no rows affected");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating loan with id " + loan.getLoanId(), e);
        }
    }

    // Approve loan
    @Override
    public void approveLoan(long loanId) {
        getLoanById(loanId).ifPresent(this::approveLoan);
    }

    @Override
    public void approveLoan(Loan loan) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            approveLoan(loan, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error approving loan with id " + loan.getLoanId(), e);
        }
    }

    @Override
    public void approveLoan(long loanId, Connection connection) {
        getLoanById(loanId, connection).ifPresent(loan -> approveLoan(loan, connection));
    }

    @Override
    public void approveLoan(Loan loan, Connection connection) {
        loan.setStatus(Loan.Status.APPROVED);
        updateLoan(loan, connection);
    }

    // Reject loan
    @Override
    public void rejectLoan(long loanId) {
        getLoanById(loanId).ifPresent(this::rejectLoan);
    }

    @Override
    public void rejectLoan(Loan loan) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            rejectLoan(loan, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error rejecting loan with id " + loan.getLoanId(), e);
        }
    }

    @Override
    public void rejectLoan(long loanId, Connection connection) {
        getLoanById(loanId, connection).ifPresent(loan -> rejectLoan(loan, connection));
    }

    @Override
    public void rejectLoan(Loan loan, Connection connection) {
        loan.setStatus(Loan.Status.REJECTED);
        updateLoan(loan, connection);
    }

    // Delete loan
    @Override
    public void deleteLoan(long loanId) {
        try (Connection connection = DBConnectionManager.getInstance().getConnection()) {
            deleteLoan(loanId, connection);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting loan with id " + loanId, e);
        }
    }

    @Override
    public void deleteLoan(Loan loan) {
        deleteLoan(loan.getLoanId());
    }

    @Override
    public void deleteLoan(long loanId, Connection connection) {
        String sql = "DELETE FROM loans WHERE loan_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, loanId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Loan with id {} deleted successfully", loanId);
            } else {
                logger.warn("No loan found with id {}", loanId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting loan with id " + loanId, e);
        }
    }

    @Override
    public void deleteLoan(Loan loan, Connection connection) {
        deleteLoan(loan.getLoanId(), connection);
    }

}
