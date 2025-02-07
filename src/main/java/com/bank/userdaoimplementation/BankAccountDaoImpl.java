package com.bank.userdaoimplementation;

import com.bank.dao.BankAccountDao;
import com.bank.model.BankAccount;
import com.bank.utils.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BankAccountDaoImpl implements BankAccountDao {

    private static final Logger logger = LogManager.getLogger(BankAccountDaoImpl.class);

    private static final String COLUMN_ACCOUNT_NUMBER = "account_number";
    private static final String COLUMN_CARD_BLOCKED = "card_blocked";
    private static final String COLUMN_REASON = "reason";
    

    public BankAccountDaoImpl() {
        String createBankAccountTableQuery = "CREATE TABLE IF NOT EXISTS BankAccounts (" +
                "account_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                COLUMN_ACCOUNT_NUMBER + " VARCHAR(20) UNIQUE NOT NULL," +
                "pin INT NOT NULL," +
                "mfa_pin INT NOT NULL," +
                "balance DECIMAL(10,2) NOT NULL," +
                "trans_id INT NOT NULL," +
                "initial_balance DECIMAL(10,2) NOT NULL," +
                "transaction_history TEXT," +
                COLUMN_CARD_BLOCKED + " BOOLEAN NOT NULL DEFAULT FALSE," +
                COLUMN_REASON + " VARCHAR(255)," +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id)" +
                ")";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createBankAccountTableQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.info("Error creating BankAccounts table: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean createBankAccount(BankAccount bankAccount) {
        String insertQuery = "INSERT INTO BankAccounts (user_id, " + COLUMN_ACCOUNT_NUMBER + ", pin, mfa_pin, balance, initial_balance, trans_id, transaction_history) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, bankAccount.getUserId());
            bankAccount.setAccountNumber(generateAccountNumber()); 
            preparedStatement.setString(2, bankAccount.getAccountNumber());
            preparedStatement.setInt(3, bankAccount.getPin());
            preparedStatement.setInt(4, bankAccount.getMfaPin());
            preparedStatement.setBigDecimal(5, bankAccount.getBalance()); 
            preparedStatement.setBigDecimal(6, bankAccount.getBalance());
            preparedStatement.setInt(7, bankAccount.getTransId());
            preparedStatement.setString(8, bankAccount.getTransactionHistory());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating bank account failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bankAccount.setAccountId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating bank account failed, no ID obtained.");
                }
            }
            return true;
        } catch (SQLException e) {
            logger.error("Error creating bank account: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }

    @Override
    public int generateNextTransId() {
        String query = "SELECT MAX(trans_id) FROM BankAccounts";
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }
    @Override
    public boolean blockCard(int userId, String accountNumber, int pin, String reason) {
        String query = "UPDATE BankAccounts SET " + COLUMN_CARD_BLOCKED + " = ?, " + COLUMN_REASON + " = ? WHERE user_id = ? AND " + COLUMN_ACCOUNT_NUMBER + " = ? AND pin = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, true);
            statement.setString(2, reason); 
            statement.setInt(3, userId);
            statement.setString(4, accountNumber);
            statement.setInt(5, pin);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public String getReasonForCardBlock(String accountNumber) {
        String query = "SELECT " + COLUMN_REASON + " FROM BankAccounts WHERE " + COLUMN_ACCOUNT_NUMBER + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(COLUMN_REASON);
                }
            }
        } catch (SQLException e) {
            logger.info("Error retrieving reason for card block: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public boolean unblockCard(int userId, String accountNumber, int pin) {
        String query = "UPDATE BankAccounts SET " + COLUMN_CARD_BLOCKED + " = ?, " + COLUMN_REASON + " = ? WHERE user_id = ? AND " + COLUMN_ACCOUNT_NUMBER + " = ? AND pin = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, false);
            statement.setString(2, "Not specified");
            statement.setInt(3, userId);
            statement.setString(4, accountNumber);
            statement.setInt(5, pin);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public BankAccount getBankAccountByAccountNumber(String accountNumber) {
        String query = "SELECT * FROM BankAccounts WHERE " + COLUMN_ACCOUNT_NUMBER + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setAccountId(resultSet.getInt("account_id"));
                    bankAccount.setUserId(resultSet.getInt("user_id"));
                    bankAccount.setAccountNumber(resultSet.getString(COLUMN_ACCOUNT_NUMBER));
                    bankAccount.setPin(resultSet.getInt("pin"));
                    bankAccount.setMfaPin(resultSet.getInt("mfa_pin"));
                    bankAccount.setBalance(resultSet.getBigDecimal("balance"));
                    bankAccount.setTransId(resultSet.getInt("trans_id"));
                    bankAccount.setinitialBalance(resultSet.getBigDecimal("initial_balance"));
                  
                    bankAccount.setTransactionHistory(resultSet.getString("transaction_history"));
                    bankAccount.setCardBlocked(resultSet.getBoolean(COLUMN_CARD_BLOCKED));
                    bankAccount.setReason(resultSet.getString(COLUMN_REASON));
                    return bankAccount;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public int getUserIdByAccountNumber(String accountNumber) {
        String query = "SELECT user_id FROM BankAccounts WHERE " + COLUMN_ACCOUNT_NUMBER + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            logger.info("Error retrieving user ID by account number: {}", e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean deleteUserByAccountNumber(String accountNumber, int pin) {
        String deleteQuery = "DELETE FROM BankAccounts WHERE " + COLUMN_ACCOUNT_NUMBER + " = ? AND pin = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setString(1, accountNumber);
            preparedStatement.setInt(2, pin);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.info("Error deleting user: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public String getAccountNumberByUserId(int userId) {
        String accountNumber = null;
        String query = "SELECT " + COLUMN_ACCOUNT_NUMBER + " FROM BankAccounts WHERE user_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    accountNumber = resultSet.getString(COLUMN_ACCOUNT_NUMBER);
                }
            }

        } catch (SQLException e) {
            logger.info("Error retrieving account number for userId {}", userId);
            e.printStackTrace();
        }

        return accountNumber;
    }
    @Override
    public Integer getPinByUserId(int userId) {
        String query = "SELECT pin FROM BankAccounts WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("pin");
                }
            }
        } catch (SQLException e) {
            logger.info("Error retrieving PIN by user ID: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BankAccount getBankAccountByUserId(int userId) {
        String query = "SELECT transaction_history, balance FROM BankAccounts WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String transactionHistory = resultSet.getString("transaction_history");
                    BigDecimal balance = resultSet.getBigDecimal("balance");
                    if (balance == null) {
                        balance = BigDecimal.ZERO;
                    }
                    return new BankAccount(userId, transactionHistory, balance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateBankAccount(BankAccount bankAccount) {
        String query = "UPDATE BankAccounts SET balance = ?, transaction_history = ? WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBigDecimal(1, bankAccount.getBalance());
            statement.setString(2, bankAccount.getTransactionHistory());
            statement.setInt(3, bankAccount.getUserId());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Integer getAccountIdByAccountNumber(String accountNumber) {
        String query = "SELECT account_id FROM BankAccounts WHERE " + COLUMN_ACCOUNT_NUMBER + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("account_id");
                }
            }
        } catch (SQLException e) {
            logger.info("Error retrieving account ID by account number: {}", e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
    @Override
    public boolean isCardBlocked(String accountNumber) {
        String query = "SELECT " + COLUMN_CARD_BLOCKED + " FROM BankAccounts WHERE " + COLUMN_ACCOUNT_NUMBER + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(COLUMN_CARD_BLOCKED);
                }
            }
        } catch (SQLException e) {
            logger.info("Error checking if card is blocked: {}", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getTransactionHistory(int userId) {
        logger.info("transaction history {}", userId);
        String query = "SELECT transaction_history FROM BankAccounts WHERE user_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String transactionHistory = resultSet.getString("transaction_history");
                    if (transactionHistory != null && !transactionHistory.isEmpty()) {
                        return transactionHistory;
                    } else {
                        return "No transactions have been recorded for the user.";
                    }
                } else {
                    return "No bank account found for the user.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error retrieving transaction history.";
        }
    }

    @Override
    public BankAccount getBankAccountByAccountNumber(int userId) {
        String query = "SELECT account_id, user_id, " + COLUMN_ACCOUNT_NUMBER + ", pin, mfa_pin, balance, trans_id, initial_balance, transaction_history, " + COLUMN_CARD_BLOCKED + ", " + COLUMN_REASON + " FROM BankAccounts WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setAccountId(resultSet.getInt("account_id"));
                    bankAccount.setUserId(resultSet.getInt("user_id"));
                    bankAccount.setAccountNumber(resultSet.getString(COLUMN_ACCOUNT_NUMBER));
                    bankAccount.setPin(resultSet.getInt("pin"));
                    bankAccount.setMfaPin(resultSet.getInt("mfa_pin"));
                    bankAccount.setBalance(resultSet.getBigDecimal("balance"));
                    bankAccount.setTransId(resultSet.getInt("trans_id"));
                    bankAccount.setinitialBalance(resultSet.getBigDecimal("initial_balance"));
                    bankAccount.setTransactionHistory(resultSet.getString("transaction_history"));
                    bankAccount.setCardBlocked(resultSet.getBoolean(COLUMN_CARD_BLOCKED));
                    bankAccount.setReason(resultSet.getString(COLUMN_REASON));
                    return bankAccount;
                }
            }
        } catch (SQLException e) {
            logger.info("Error retrieving bank account by account number: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
