package com.virtusa.userdaoimplementation;

import com.bank.dao.UserDao;
import com.bank.model.User;
import com.bank.utils.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_BALANCE = "balance";

    public UserDaoImpl() {
        String createUserTableQuery = "CREATE TABLE IF NOT EXISTS Users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "phone_number VARCHAR(20) NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL" +
                ")";
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createUserTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int saveUser(User user) {
        String query = "INSERT INTO Users (name, phone_number, email) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getPhoneNumber());
            preparedStatement.setString(3, user.getEmail());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                ResultSet rs = preparedStatement.getGeneratedKeys();
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                    return user.getUserId();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public User findUserByEmail(String email) {
        User user = null;
        String query = "SELECT * FROM Users WHERE " + COLUMN_EMAIL + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = new User();
                user.setUserId(resultSet.getInt(COLUMN_USER_ID));
                user.setName(resultSet.getString(COLUMN_NAME));
                user.setPhoneNumber(resultSet.getString(COLUMN_PHONE_NUMBER));
                user.setEmail(resultSet.getString(COLUMN_EMAIL));
                user.setBalance(resultSet.getBigDecimal(COLUMN_BALANCE));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public boolean transfer(User fromUser, User toUser, BigDecimal amount) {
        if (fromUser.getBalance().compareTo(amount) < 0) {
            return false;
        }

        Connection connection = null;
        PreparedStatement withdrawStatement = null;
        PreparedStatement depositStatement = null;

        try {
            connection = DatabaseManager.getConnection();
            connection.setAutoCommit(false);

            String withdrawQuery = "UPDATE Users SET " + COLUMN_BALANCE + " = " + COLUMN_BALANCE + " - ? WHERE " + COLUMN_USER_ID + " = ?";
            withdrawStatement = connection.prepareStatement(withdrawQuery);
            withdrawStatement.setBigDecimal(1, amount);
            withdrawStatement.setInt(2, fromUser.getUserId());
            int rowsAffectedWithdraw = withdrawStatement.executeUpdate();

            String depositQuery = "UPDATE Users SET " + COLUMN_BALANCE + " = " + COLUMN_BALANCE + " + ? WHERE " + COLUMN_USER_ID + " = ?";
            depositStatement = connection.prepareStatement(depositQuery);
            depositStatement.setBigDecimal(1, amount);
            depositStatement.setInt(2, toUser.getUserId());
            int rowsAffectedDeposit = depositStatement.executeUpdate();

            if (rowsAffectedWithdraw == 1 && rowsAffectedDeposit == 1) {
                fromUser.setBalance(fromUser.getBalance().subtract(amount));
                toUser.setBalance(toUser.getBalance().add(amount));
                connection.commit();
                return true;
            } else {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            // Close statements and connection
            try {
                if (withdrawStatement != null) {
                    withdrawStatement.close();
                }
                if (depositStatement != null) {
                    depositStatement.close();
                }
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM Users";
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt(COLUMN_USER_ID));
                user.setName(resultSet.getString(COLUMN_NAME));
                user.setPhoneNumber(resultSet.getString(COLUMN_PHONE_NUMBER));
                user.setEmail(resultSet.getString(COLUMN_EMAIL));
                user.setBalance(resultSet.getBigDecimal(COLUMN_BALANCE));
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    @Override
    public boolean withdraw(User user, BigDecimal amount) {
        String query = "UPDATE Users SET " + COLUMN_BALANCE + " = " + COLUMN_BALANCE + " - ? WHERE " + COLUMN_USER_ID + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, amount);
            preparedStatement.setInt(2, user.getUserId());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                user.setBalance(user.getBalance().subtract(amount));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deposit(User user, BigDecimal amount) {
        String query = "UPDATE Users SET " + COLUMN_BALANCE + " = " + COLUMN_BALANCE + " + ? WHERE " + COLUMN_USER_ID + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, amount);
            preparedStatement.setInt(2, user.getUserId());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                user.setBalance(user.getBalance().add(amount));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public User getUserByEmail(String email) {
        User user = null;
        String query = "SELECT * FROM Users WHERE " + COLUMN_EMAIL + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    user = new User();
                    user.setUserId(resultSet.getInt(COLUMN_USER_ID));
                    user.setName(resultSet.getString(COLUMN_NAME));
                    user.setPhoneNumber(resultSet.getString(COLUMN_PHONE_NUMBER));
                    user.setEmail(resultSet.getString(COLUMN_EMAIL));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public User getUserById(int userId) {
        User user = null;
        String query = "SELECT * FROM Users WHERE " + COLUMN_USER_ID + " = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    user = new User();
                    user.setUserId(resultSet.getInt(COLUMN_USER_ID));
                    user.setName(resultSet.getString(COLUMN_NAME));
                    user.setPhoneNumber(resultSet.getString(COLUMN_PHONE_NUMBER));
                    user.setEmail(resultSet.getString(COLUMN_EMAIL));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}

