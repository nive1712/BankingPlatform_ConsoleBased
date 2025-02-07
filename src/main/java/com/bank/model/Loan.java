package com.bank.model;

import java.math.BigDecimal;
import java.util.Date;

public class Loan {
    private int loanId;
    private int userId;
    private int bankAccountId;
    private BigDecimal totalPayment;
    private BigDecimal downPayment;
    private BigDecimal interestRate;
    private int tenureYears;
    private BigDecimal emiAmount;
    private Date sanctionDate;
    


	public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(int bankAccountId2) {
        this.bankAccountId = bankAccountId2;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public int getTenureYears() {
        return tenureYears;
    }

    public void setTenureYears(int tenureYears) {
        this.tenureYears = tenureYears;
    }

    public BigDecimal getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(BigDecimal emiAmount) {
        this.emiAmount = emiAmount;
    }

    public Date getSanctionDate() {
        return sanctionDate;
    }

    public void setSanctionDate(Date sanctionDate) {
        this.sanctionDate = sanctionDate;
    }
}

