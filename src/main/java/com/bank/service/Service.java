package com.bank.service;

import com.bank.model.User;

import java.math.BigDecimal;
import java.util.List;

import com.bank.model.BankAccount;


public interface Service {
  int registerUser(User user, String password);
  User loginUser(String email, String password);
  void createBankAccount(BankAccount bankAccount);
  boolean deposit(int userId, double amount);
  boolean withdraw(int userId, double amount);
 
  boolean netBankingDeposit(int userId, double amount);
  List<String> getNotification(int userId);
  String getTransactionHistory(int userId);
  boolean authenticate(String accountNumber, int pin);
  int getUserIdByAccountNumber(String accountNumber);
 
	boolean transfer(int userId, String senderAccountNumber, String recipientAccountNumber, BigDecimal transferAmount);
	String getAccountNumberByUserId(int userId);
	boolean isCardBlocked(String accountNumber);
	Integer getPinByUserId(int userId);

    
}
