package com.bank.main;

import com.bank.model.User;
import com.bank.model.BankAccount;
import com.bank.model.Loan;
import com.bank.service.Service;
import com.bank.dao.BankAccountDao;
import com.virtusa.serviceimplementation.ServiceImpl;
import com.virtusa.userdaoimplementation.BankAccountDaoImpl;
import com.virtusa.userdaoimplementation.LoanDaoImpl;
import com.bank.dao.LoanDao;

import java.math.BigDecimal;
import java.util.Scanner;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserRegistration {
    private static final Logger logger = LogManager.getLogger(UserRegistration.class);
    private static final Service service = new ServiceImpl();
    private static final BankAccountDao bankAccountDao = new BankAccountDaoImpl();
    private static final LoanDao loanDao = new LoanDaoImpl();
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean bankAccountsTableCreated = false;
    private static final String INVALID_CHOICE_MESSAGE = "Invalid choice.";
    private static final String ENTER_ACCOUNT_NUMBER = "Enter your account number: ";

    public static void main(String[] args) {
        createBankAccountsTableIfNeeded();
        logger.info("Welcome to the Bank Registration System");
        logger.info("1. Register");
        logger.info("2. Net Banking");
        logger.info("3. ATM");
        int choice = scanner.nextInt();
        scanner.nextLine();
        int userId = 0;
        switch (choice) {
            case 1:
                registerUser();
                break;
            case 2:
                loginUser();
                break;
            case 3:
                logger.info("Enter your user ID: ");
                userId = scanner.nextInt();
                atmOptions(userId);
                break;
            default:
                logger.info(INVALID_CHOICE_MESSAGE);
        }
    }

    private static void createBankAccountsTableIfNeeded() {
        if (!bankAccountsTableCreated) {
            
            bankAccountsTableCreated = true;
        }
    }

    private static void registerUser() {
        User user = new User();

        logger.info("Enter your name: ");
        user.setName(scanner.nextLine());

        logger.info("Enter your phone number: ");
        user.setPhoneNumber(scanner.nextLine());

        logger.info("Enter your email: ");
        user.setEmail(scanner.nextLine());

        logger.info("Set your password: ");
        String password = scanner.nextLine();

        int userId = service.registerUser(user, password);

        if (userId != -1) {
            logger.info("User registered successfully with userId:{} ", userId);
            createBankAccount(userId);
            selectAccountType(userId);
        } else {
            logger.info("User registration failed.");
        }
    }

    private static Random random = new Random();

    private static void createBankAccount(int userId) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setUserId(userId);
        bankAccount.setAccountNumber(bankAccountDao.generateAccountNumber());
        bankAccount.setPin(random.nextInt(10000)); 
        bankAccount.setMfaPin(1111 + userId);
        bankAccount.setTransId(bankAccountDao.generateNextTransId());
        bankAccount.setBalance(BigDecimal.valueOf(500));
        bankAccount.setinitialBalance(BigDecimal.valueOf(500));

        bankAccount.setTransactionHistory("Initial deposit: 500");

        if (bankAccountDao.createBankAccount(bankAccount)) {
            logger.info("Bank account created successfully with account number: {}", bankAccount.getAccountNumber());
        } else {
            logger.info("Failed to create bank account.");
        }
    }

    private static void selectAccountType(int userId) {
        logger.info("Select account type:");
        logger.info("1. ATM");
        logger.info("2. Net Banking");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                atmOptions(userId);
                break;
            case 2:
                netBankingOptions(userId);
                break;
            default:
                logger.info(INVALID_CHOICE_MESSAGE);
        }
    }

    private static void loginUser() {
        logger.info("Enter your email: ");
        String email = scanner.nextLine();

        logger.info("Enter your password: ");
        String password = scanner.nextLine();

        User user = service.loginUser(email, password);
        if (user != null) {
            logger.info("Login successful!");
            netBankingOptions(user.getUserId());
        } else {
            logger.info("Invalid credentials.");
        }
    }

    private static void netBankingOptions(int userId) {
        boolean exit = false;
        while (!exit) {
            logger.info("Net Banking options:");
            logger.info("2. Deposit");
            logger.info("3. Transfer");
            logger.info("4. Block");
            logger.info("5. Unblock");
            logger.info("6. My Transaction");
            logger.info("7. Loans");
            logger.info("8. Exit");
            logger.info("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 2:
                    handleDeposit(userId);
                    break;
                case 3:
                    handleTransfer(userId);
                    break;
                case 4:
                    logger.info(ENTER_ACCOUNT_NUMBER);
                    String blockAccountNumber = scanner.nextLine();
                    logger.info("Enter your PIN: ");
                    int blockPin = scanner.nextInt();
                    scanner.nextLine();

                    logger.info("Enter reason for blocking: ");
                    String reason = scanner.nextLine();

                    logger.info("Are you sure you want to block your card with the following reason?");
                    logger.info("Reason: {}", reason);
                    logger.info("1) Yes - Block");
                    logger.info("2) No - Cancel");

                    int blockChoice = scanner.nextInt();
                    scanner.nextLine(); 

                    if (blockChoice == 1) {
                        boolean blockSuccess = bankAccountDao.blockCard(userId, blockAccountNumber, blockPin, reason);
                        if (blockSuccess) {
                            logger.info("Your card has been blocked successfully.");
                        } else {
                            logger.info("Unable to block your card.");
                        }
                    } else {
                        logger.info("Card blocking canceled.");
                    }
                    break;
                case 5:
                    logger.info(ENTER_ACCOUNT_NUMBER);
                    String unblockAccountNumber = scanner.nextLine();
                    logger.info("Enter your PIN: ");
                    int unblockPin = scanner.nextInt();
                    scanner.nextLine(); 
                    logger.info("Are you sure you want to unblock your card?");
                    logger.info("1) Yes - Unblock");
                    logger.info("2) No - Cancel");

                    int unblockChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (unblockChoice == 1) {
                        boolean unblockSuccess = bankAccountDao.unblockCard(userId, unblockAccountNumber, unblockPin);
                        if (unblockSuccess) {
                            logger.info("Your card has been unblocked successfully.");
                        } else {
                            logger.info("Unable to unblock your card.");
                        }
                    } else {
                        logger.info("Card unblocking canceled.");
                    }
                    break;
                case 6:
                    List<String> notifications = service.getNotification(userId);
                    for (String notification : notifications) {
                        logger.info(notification);
                    }
                    break;
                case 7:
                    handleLoanPayment(userId);
                    break;
                case 8:
                    exit = true;
                    break;
                default:
                    logger.info(INVALID_CHOICE_MESSAGE);
            }
        }
    }

    private static void handleDeposit(int userId) {
        logger.info("Enter amount to deposit: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        BankAccount bankAccount = bankAccountDao.getBankAccountByUserId(userId);
        if (bankAccount != null) {
            String transactionType = "Deposit";
            String transactionHistory = bankAccount.getTransactionHistory() + "\n" +
                    transactionType + ": " + amount.toString();

            bankAccount.setTransactionHistory(transactionHistory);

            BigDecimal newBalance = bankAccount.getBalance().add(amount);
            bankAccount.setBalance(newBalance);

            if (bankAccountDao.updateBankAccount(bankAccount)) {
                logger.info("Deposit successful. New balance:{} ", newBalance);
            } else {
                logger.info("Failed to deposit.");
            }
        } else {
            logger.info("Bank account not found.");
        }
    }

    private static void handleTransfer(int userId) {
        logger.info(ENTER_ACCOUNT_NUMBER);
        String senderAccountNumber = scanner.nextLine();

        logger.info("Enter recipient's account number: ");
        String recipientAccountNumber = scanner.nextLine();

        logger.info("Enter amount to transfer: ");
        BigDecimal transferAmount = new BigDecimal(scanner.nextLine());

        boolean success = service.transfer(userId, senderAccountNumber, recipientAccountNumber, transferAmount);
        if (success) {
            logger.info("Transfer successful. Amount: {}", transferAmount);
        } else {
            logger.info("Transfer failed. Check balance or recipient's account number.");
        }
    }

    private static void handleLoanPayment(int userId) {
        Loan loan = new Loan();
        loan.setUserId(userId);

        BankAccount bankAccount = bankAccountDao.getBankAccountByAccountNumber(userId);
        int bankAccountId = bankAccount.getAccountId();
        if (bankAccountId == 0) {
            logger.info("No bank account found for the user.");
            return;
        }
        loan.setBankAccountId(bankAccountId);

        logger.info("Select loan type:");
        logger.info("1. House Loan (Home Loan)");
        logger.info("2. Car Loan");
        logger.info("3. Bike Loan");
        logger.info("4. Personal Loan");
        logger.info("Enter your choice: ");
        int loanTypeChoice = scanner.nextInt();
        scanner.nextLine();

        logger.info("Enter Total Payment Amount: ");
        BigDecimal totalPaymentAmount = scanner.nextBigDecimal();

        logger.info("Enter Down Payment Amount: ");
        BigDecimal downPaymentAmount = scanner.nextBigDecimal();
        BigDecimal loanAmount = totalPaymentAmount.subtract(downPaymentAmount);

        loan.setTotalPayment(totalPaymentAmount);
        loan.setDownPayment(downPaymentAmount);
        loan.setEmiAmount(loanAmount);

        logger.info("Enter tenure years: ");
        int tenureYears = scanner.nextInt();

        double interestRate;
        if (loanTypeChoice == 1) {
            if (tenureYears >= 5 && tenureYears <= 15) {
                interestRate = 8.0 + (tenureYears - 5) * 1.0;
            } else {
                logger.info("Invalid tenure. Tenure must be between 5 and 15 years.");
                return;
            }
        } else if (loanTypeChoice == 2) {
            if (tenureYears >= 2 && tenureYears <= 8) {
                interestRate = 7.0 + (tenureYears - 2) * 0.5;
            } else {
                logger.info("Invalid tenure. Tenure must be between 2 and 8 years.");
                return;
            }
        } else if (loanTypeChoice == 3) {
            if (tenureYears >= 2 && tenureYears <= 8) {
                interestRate = 6.0 + (tenureYears - 2) * 0.5;
            } else {
                logger.info("Invalid tenure. Tenure must be between 2 and 8 years.");
                return;
            }
        } else if (loanTypeChoice == 4) {
            if (tenureYears >= 1 && tenureYears <= 5) {
                interestRate = 10.0 + (tenureYears - 1) * 1.0;
            } else {
                logger.info("Invalid tenure. Tenure must be between 1 and 5 years.");
                return;
            }
        } else {
            logger.info("Invalid loan type.");
            return;
        }

        loan.setInterestRate(BigDecimal.valueOf(interestRate));
        loan.setTenureYears(tenureYears);

        double monthlyInterestRate = interestRate / 12 / 100;
        int numberOfPayments = tenureYears * 12;
        double emi = (loanAmount.doubleValue() * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, numberOfPayments)) /
                (Math.pow(1 + monthlyInterestRate, numberOfPayments) - 1);
        loan.setEmiAmount(BigDecimal.valueOf(emi));

        double totalInterestPayable = (emi * numberOfPayments) - loanAmount.doubleValue();

        logger.info("Loan EMI:{} ₹", Math.round(emi));
        logger.info("Total Interest Payable:{} ₹", Math.round(totalInterestPayable));
        logger.info("Total Payment (Principal + Interest): {}₹", Math.round(loanAmount.doubleValue() + totalInterestPayable));

        loan.setSanctionDate(new java.util.Date());

        if (loanDao.createLoan(loan)) {
            logger.info("Loan created successfully.");
        } else {
            logger.info("Failed to create loan.");
        }
    }

    private static void atmOptions(int userId) {
        logger.info("Welcome to ATM, Please enter credentials to proceed!");
        scanner.nextLine();

        logger.info("Enter your account number:");
        String accNo = scanner.nextLine();
        String accountNo = service.getAccountNumberByUserId(userId);

        if (accNo.equals(accountNo)) {
            logger.info("Enter your card pin:");
            Integer atmPin = scanner.nextInt();
            Integer pin = service.getPinByUserId(userId);

            if (atmPin.equals(pin)) {
                boolean exit = false;

                while (!exit) {
                    logger.info("ATM options:");
                    logger.info("1. Deposits");
                    logger.info("2. Withdraw");
                    logger.info("3. Transfer");
                    logger.info("4. Transaction History");
                    logger.info("5. Exit");

                    int choice = scanner.nextInt();
                    scanner.nextLine(); 

                    switch (choice) {
                        case 1:
                            atmDeposit(userId);
                            break;
                        case 2:
                            atmWithdraw(userId);
                            break;
                        case 3:
                            atmTransfer(userId);
                            break;
                        case 4:
                            atmTransactionHistory(userId);
                            break;
                        case 5:
                            exit = true;
                            break;
                        default:
                            logger.info("Invalid choice. Please try again.");
                            break;
                    }
                }
            } else {
                logger.info("Invalid pin, try again!");
            }
        } else {
            logger.info("Invalid account number, try again!");
        }
    }

    private static void atmDeposit(int userId) {
        String accountNumber = service.getAccountNumberByUserId(userId);
        boolean cardBlocked = service.isCardBlocked(accountNumber);
        if (cardBlocked) {
            logger.info("Your card has been blocked. Deposit transaction not allowed.");
            return;
        }
        logger.info("Enter the number of notes for each denomination to deposit:");
        logger.info("100s: ");
        int hundreds = scanner.nextInt();
        logger.info("500s: ");
        int fiveHundreds = scanner.nextInt();
        logger.info("2000s: ");
        int twoThousands = scanner.nextInt();

        double amount = (hundreds * 100.0) + (fiveHundreds * 500.0) + (twoThousands * 2000.0);
        scanner.nextLine();

        boolean success = service.deposit(userId, amount);
        if (success) {
            logger.info("Deposit successful. Amount: {}", amount);
        } else {
            logger.info("Deposit failed.");
        }
    }

    private static void atmWithdraw(int userId) {
        String accountNumber = service.getAccountNumberByUserId(userId);
        boolean cardBlocked = service.isCardBlocked(accountNumber);
        if (cardBlocked) {
            logger.info("Your card has been blocked. Withdraw transaction not allowed.");
            return;
        }
        logger.info("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        boolean success = service.withdraw(userId, amount);
        if (success) {
            logger.info("Withdrawal successful. Amount: {}", amount);
            logger.info("Collect your cash:");
            int twoThousands = (int) amount / 2000;
            amount %= 2000;
            int fiveHundreds = (int) amount / 500;
            amount %= 500;
            int hundreds = (int) amount / 100;
            logger.info("2000s:{} ", twoThousands);
            logger.info("500s:{} ", fiveHundreds);
            logger.info("100s:{} ", hundreds);
        } else {
            logger.info("Withdrawal failed. Check balance or withdrawal limit.");
        }
    }

    private static void atmTransfer(int userId) {
        String accountNumber = service.getAccountNumberByUserId(userId);
        boolean cardBlocked = service.isCardBlocked(accountNumber);
        if (cardBlocked) {
            logger.info("Your card has been blocked. Transfer transaction not allowed.");
            return;
        }
        logger.info(ENTER_ACCOUNT_NUMBER);
        String senderAccountNumber = scanner.nextLine();

        logger.info("Enter recipient's account number: ");
        String recipientAccountNumber = scanner.nextLine();

        logger.info("Enter amount to transfer: ");
        BigDecimal transferAmount = new BigDecimal(scanner.nextLine());

        boolean success = service.transfer(userId, senderAccountNumber, recipientAccountNumber, transferAmount);
        if (success) {
            logger.info("Transfer successful. Amount: {}", transferAmount);
        } else {
            logger.info("Transfer failed. Check balance or recipient's account number.");
        }
    }

    private static void atmTransactionHistory(int userId) {
        String accountNumber = service.getAccountNumberByUserId(userId);
        boolean cardBlocked = service.isCardBlocked(accountNumber);
        if (cardBlocked) {
            logger.info("Your card has been blocked. Transaction History visibility is closed.");
            return;
        }
        logger.info("Transaction Details: {}", userId);
        String history = service.getTransactionHistory(userId);
        logger.info(history);
    }
}

