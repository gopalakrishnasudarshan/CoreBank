package com.corebank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transfer {

    private long transferId;
    private Account fromAccount;
    private Account toAccount;
    private BigDecimal amount;
    private LocalDateTime timestamp;


    public Transfer(Account fromAccount, Account toAccount, BigDecimal amount, LocalDateTime timestamp) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.timestamp = timestamp;
    }


    public Transfer(long transferId, Account fromAccount, Account toAccount, BigDecimal amount, LocalDateTime timestamp) {
        this(fromAccount, toAccount, amount, timestamp);
        this.transferId = transferId;
    }


    public long getTransferId() { return transferId; }
    public void setTransferId(long transferId) { this.transferId = transferId; }

    public Account getFromAccount() { return fromAccount; }
    public void setFromAccount(Account fromAccount) { this.fromAccount = fromAccount; }

    public Account getToAccount() { return toAccount; }
    public void setToAccount(Account toAccount) { this.toAccount = toAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Transfer{" +
                "transferId=" + transferId +
                ", fromAccount=" + fromAccount +
                ", toAccount=" + toAccount +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
