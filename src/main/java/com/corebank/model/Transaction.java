package com.corebank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    public enum Type { DEPOSIT, WITHDRAWAL }

    private long transactionId;
    private Account account;
    private Type type;
    private BigDecimal amount;
    private LocalDateTime timestamp;


    public Transaction(Account account, Type type, BigDecimal amount, LocalDateTime timestamp) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }


    public Transaction(long transactionId, Account account, Type type, BigDecimal amount, LocalDateTime timestamp) {
        this(account, type, amount, timestamp);
        this.transactionId = transactionId;
    }


    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", account=" + account +
                ", type=" + type +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
