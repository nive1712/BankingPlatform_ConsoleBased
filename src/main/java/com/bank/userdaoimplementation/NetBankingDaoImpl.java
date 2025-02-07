package com.virtusa.userdaoimplementation;

import com.bank.dao.NetBankingDao;
import com.bank.model.NetBanking;
import com.bank.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NetBankingDaoImpl implements NetBankingDao {

    public NetBankingDaoImpl() {

        String createNetBankingTableQuery = "CREATE TABLE IF NOT EXISTS NetBanking (" +
                "net_banking_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id)" +
                ")";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createNetBankingTableQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveNetBanking(NetBanking netBanking) {
        String sql = "INSERT INTO NetBanking (user_id, password) VALUES (?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, netBanking.getUserId());
            preparedStatement.setString(2, netBanking.getPassword());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public NetBanking getNetBankingByUserId(int userId) {
        String query = "SELECT * FROM NetBanking n JOIN Users u ON n.user_id = u.user_id WHERE n.user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    NetBanking netBanking = new NetBanking();
                    netBanking.setNetBankingId(resultSet.getInt("net_banking_id"));
                    netBanking.setUserId(resultSet.getInt("user_id"));
                    netBanking.setPassword(resultSet.getString("password"));
                    
                    return netBanking;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
