package com.corebank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public enum AccountType {
        SAVINGS,
        CHECKING
    }
    private long accountId;
    private Customer customer;
    private AccountType accountType;
    private BigDecimal balance;
    private Status status;
    private LocalDateTime createdAt;

    public Account( Customer customer, AccountType accountType, BigDecimal balance, Status status, LocalDateTime createdAt) {
        this.customer = customer;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Account(long accountId, Customer customer, AccountType accountType, BigDecimal balance, Status status, LocalDateTime createdAt)
    {
        this (customer,accountType,balance,status,createdAt);
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", customer=" + customer +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
