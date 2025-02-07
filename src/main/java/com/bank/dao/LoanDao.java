package com.bank.dao;


import com.bank.model.Loan;

public interface LoanDao {
    boolean createLoan(Loan loan);
    Loan getLoanById(int loanId);

}

