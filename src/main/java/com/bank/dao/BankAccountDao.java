package com.bank.dao;


import com.bank.model.BankAccount;

public interface BankAccountDao {
  String getTransactionHistory(int userId);
  BankAccount getBankAccountByAccountNumber(int userId); 
   
  int getUserIdByAccountNumber(String accountNumber);
  boolean deleteUserByAccountNumber(String accountNumber, int pin);
   
	Integer getAccountIdByAccountNumber(String accountNumber);
	BankAccount getBankAccountByAccountNumber(String recipientAccountNumber);
	String generateAccountNumber();
	int generateNextTransId();

	BankAccount getBankAccountByUserId(int userId);
	boolean createBankAccount(BankAccount bankAccount);
	boolean updateBankAccount(BankAccount bankAccount);
	String getAccountNumberByUserId(int userId);
	boolean blockCard(int userId, String accountNumber, int pin, String reason); 
	boolean unblockCard(int userId, String accountNumber, int pin);
	String getReasonForCardBlock(String accountNumber);
		boolean isCardBlocked(String accountNumber);
		Integer getPinByUserId(int userId);
		
}
