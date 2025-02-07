package com.bank.serviceimplementation;

import com.bank.dao.UserDao;

import com.bank.dao.BankAccountDao;
import com.bank.dao.NetBankingDao;
import com.bank.model.User;
import com.bank.model.BankAccount;
import com.bank.model.NetBanking;
import com.bank.service.Service;
import com.bank.userdaoimplementation.BankAccountDaoImpl;
import com.bank.userdaoimplementation.NetBankingDaoImpl;
import com.bank.userdaoimplementation.UserDaoImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class ServiceImpl implements Service {
	private static final Logger logger = LogManager.getLogger(ServiceImpl.class);

    private UserDao userDao;
    private NetBankingDao netBankingDao;
    private BankAccountDao bankAccountDao;

    public ServiceImpl() {
        this.userDao = new UserDaoImpl();
        this.netBankingDao = new NetBankingDaoImpl();
        this.bankAccountDao = new BankAccountDaoImpl();
    }

    @Override
    public int registerUser(User user, String password) {
        int userId = userDao.saveUser(user);
        if (userId != -1) {
            NetBanking netBanking = new NetBanking();
            netBanking.setUserId(userId);
            netBanking.setPassword(password);
            netBankingDao.saveNetBanking(netBanking);
        }
        return userId;
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userDao.getUserByEmail(email);
        if (user != null) {
            NetBanking netBanking = netBankingDao.getNetBankingByUserId(user.getUserId());
            if (netBanking != null && netBanking.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void createBankAccount(BankAccount bankAccount) {
        bankAccount.setBalance(BigDecimal.valueOf(500));
        bankAccount.setinitialBalance(BigDecimal.valueOf(500));
        bankAccount.setTransactionHistory("Initial deposit: 500 to account number " + bankAccount.getAccountNumber());
        bankAccountDao.createBankAccount(bankAccount);
        logger.info("Bank account created successfully with initial deposit of 500!");
    }

    @Override
    public boolean deposit(int userId, double amount) {
        BankAccount bankAccount = bankAccountDao.getBankAccountByUserId(userId);
        if (bankAccount != null) {
            BigDecimal depositAmount = BigDecimal.valueOf(amount);
            BigDecimal newBalance = bankAccount.getBalance().add(depositAmount);
            bankAccount.setBalance(newBalance);
            bankAccount.setinitialBalance(newBalance);

            String transactionHistory = bankAccount.getTransactionHistory();
            String updatedTransactionHistory = transactionHistory + "\n" + new Date() + ": Deposit " + amount ;
            bankAccount.setTransactionHistory(updatedTransactionHistory);

            return bankAccountDao.updateBankAccount(bankAccount);
        }
        return false;
    }

    @Override
    public boolean withdraw(int userId, double amount) {
        BankAccount bankAccount = bankAccountDao.getBankAccountByUserId(userId);
        if (bankAccount != null) {
            BigDecimal withdrawAmount = BigDecimal.valueOf(amount);
            BigDecimal newBalance = bankAccount.getBalance().subtract(withdrawAmount);

            if (newBalance.compareTo(BigDecimal.ZERO) >= 0) {
                bankAccount.setBalance(newBalance);
                bankAccount.setinitialBalance(newBalance);

                String transactionHistory = bankAccount.getTransactionHistory();
                String updatedTransactionHistory = transactionHistory + "\n" + new Date() + ": Withdraw " + amount ;
                bankAccount.setTransactionHistory(updatedTransactionHistory);

                return bankAccountDao.updateBankAccount(bankAccount);
            } else {
                logger.info("Insufficient balance.");
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean netBankingDeposit(int userId, double amount) {
        return deposit(userId, amount);
    }

    @Override
    public List<String> getNotification(int userId) {
        List<String> notifications = new ArrayList<>();
        notifications.add("Notification: Your recent transaction history:");
        notifications.add(getTransactionHistory(userId));

        return notifications;
    }

    @Override
    public String getTransactionHistory(int userId) {
        BankAccount bankAccount = bankAccountDao.getBankAccountByUserId(userId);
        if (bankAccount != null) {
            String transactionHistory = bankAccount.getTransactionHistory();
            if (transactionHistory != null && !transactionHistory.isEmpty()) {
                return transactionHistory;
            } else {
                return "No transactions have been recorded for the user.";
            }
        } else {
            return "No bank account found for the user.";
        }
    }


    @Override
    public boolean authenticate(String accountNumber, int pin) {
        int userId = getUserIdByAccountNumber(accountNumber);
        if (userId != -1) {
            BankAccount bankAccount = bankAccountDao.getBankAccountByUserId(userId);
            if (bankAccount != null && bankAccount.getPin() == pin) {
                return true;
            }

        }
        return false;
    }
    @Override
    public Integer getPinByUserId(int userId) {
        return bankAccountDao.getPinByUserId(userId);
    }
   
    @Override
    public int getUserIdByAccountNumber(String accountNumber) {
        BankAccount bankAccount = bankAccountDao.getBankAccountByAccountNumber(accountNumber);
        if (bankAccount != null) {
            return bankAccount.getUserId();
        }
        return -1;
    }
    @Override
    public String getAccountNumberByUserId(int userId) {
        BankAccount bank = bankAccountDao.getBankAccountByAccountNumber(userId);
        if (bank != null) {
            return bank.getAccountNumber();
        }
        return null;
    }
    

    @Override
    public boolean transfer(int senderId, String senderAccountNumber, String recipientAccountNumber, BigDecimal transferAmount) {
        
    	
        BankAccount senderAccount = bankAccountDao.getBankAccountByAccountNumber(senderAccountNumber);
        BankAccount receiverAccount = bankAccountDao.getBankAccountByAccountNumber(recipientAccountNumber);

        if (senderAccount != null && receiverAccount != null) {
            BigDecimal senderBalance = senderAccount.getBalance();
            BigDecimal receiverBalance = receiverAccount.getBalance();

            if (senderBalance.compareTo(transferAmount) >= 0) {
                BigDecimal newSenderBalance = senderBalance.subtract(transferAmount);
                BigDecimal newReceiverBalance = receiverBalance.add(transferAmount);

                senderAccount.setBalance(newSenderBalance);
                receiverAccount.setBalance(newReceiverBalance);

                String senderTransactionHistory = senderAccount.getTransactionHistory() +
                        "\n" + new Date() + ": Transferred " + transferAmount + " to account number " + recipientAccountNumber;
                String receiverTransactionHistory = receiverAccount.getTransactionHistory() +
                        "\n" + new Date() + ": Received " + transferAmount + " from account number " + senderAccountNumber;

                senderAccount.setTransactionHistory(senderTransactionHistory);
                receiverAccount.setTransactionHistory(receiverTransactionHistory);

                boolean senderUpdate = bankAccountDao.updateBankAccount(senderAccount);
                boolean receiverUpdate = bankAccountDao.updateBankAccount(receiverAccount);

                return senderUpdate && receiverUpdate;
            } else {
                logger.info("Insufficient balance.");
            }
        } else {
            logger.info("Sender or receiver account not found.");
        }
        return false;
    }

    @Override
    public boolean isCardBlocked(String accountNumber) {
        return bankAccountDao.isCardBlocked(accountNumber);
    }
}
