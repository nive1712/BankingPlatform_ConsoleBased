package com.bank.model;

import java.math.BigDecimal;

public class BankAccount {
    private int accountId;
    private int userId;
    private String accountNumber;
    private int mfaPin;
    
    private BigDecimal initialBalance;
    private int pin;
    private BigDecimal balance;
    private int transId;
    
    private String transactionHistory;
  
    private boolean cardBlocked;
    private String reason;

    public BankAccount() {
    }

    public BankAccount(int userId, String transactionHistory, BigDecimal balance) {
        this.userId = userId;
        this.transactionHistory = transactionHistory;
        this.balance = balance;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public int getTransId() {
        return transId;
    }

    public void setTransId(int transId) {
        this.transId = transId;
    }

   

    public int getMfaPin() {
        return mfaPin;
    }

    public void setMfaPin(int mfaPin) {
        this.mfaPin = mfaPin;
    }

  
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setinitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(String transactionHistory) {
        this.transactionHistory = transactionHistory;
    }

    
    public boolean isCardBlocked() {
        return cardBlocked;
    }

    public void setCardBlocked(boolean cardBlocked) {
        this.cardBlocked = cardBlocked;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
