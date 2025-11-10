package com.corebank.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Loan {

    public enum Status { PENDING, APPROVED, REJECTED, PAID }

    private long loanId;
    private Customer customer;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Status status;


    public Loan(Customer customer, BigDecimal amount, BigDecimal interestRate, LocalDate startDate, LocalDate endDate, Status status) {
        this.customer = customer;
        this.amount = amount;
        this.interestRate = interestRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }


    public Loan(long loanId, Customer customer, BigDecimal amount, BigDecimal interestRate, LocalDate startDate, LocalDate endDate, Status status) {
        this(customer, amount, interestRate, startDate, endDate, status);
        this.loanId = loanId;
    }


    public long getLoanId() { return loanId; }
    public void setLoanId(long loanId) { this.loanId = loanId; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "Loan{" +
                "loanId=" + loanId +
                ", customer=" + customer +
                ", amount=" + amount +
                ", interestRate=" + interestRate +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
