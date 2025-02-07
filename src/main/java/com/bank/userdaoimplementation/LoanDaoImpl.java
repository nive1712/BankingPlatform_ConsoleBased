package com.bank.userdaoimplementation;

import com.bank.dao.LoanDao;
import com.bank.model.Loan;
import com.bank.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


public class LoanDaoImpl implements LoanDao {

    public LoanDaoImpl() {
        createLoanTable();
    }

    private void createLoanTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS Loans (" +
                "loan_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "bank_account_id INT," +
                "total_payment DECIMAL(15, 2)," +
                "down_payment DECIMAL(15, 2)," +
                "interest_rate DECIMAL(5, 2)," +
                "tenure_years INT," +
                "emi_amount DECIMAL(15, 2)," +
                "sanction_date DATE," +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id)," +
                "FOREIGN KEY (bank_account_id) REFERENCES BankAccounts(account_id)" +
                ")";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean createLoan(Loan loan) {
        String insertLoanQuery = "INSERT INTO Loans (user_id, bank_account_id, total_payment, down_payment, interest_rate, tenure_years, emi_amount, sanction_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertLoanQuery)) {

            preparedStatement.setInt(1, loan.getUserId());
            preparedStatement.setInt(2, loan.getBankAccountId());
            preparedStatement.setBigDecimal(3, loan.getTotalPayment());
            preparedStatement.setBigDecimal(4, loan.getDownPayment());
            preparedStatement.setBigDecimal(5, loan.getInterestRate());
            preparedStatement.setInt(6, loan.getTenureYears());
            preparedStatement.setBigDecimal(7, loan.getEmiAmount());
            
            if (loan.getSanctionDate() != null) {
                preparedStatement.setDate(8, new java.sql.Date(loan.getSanctionDate().getTime()));
            } else {
                preparedStatement.setNull(8, Types.DATE); 
            }
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Loan getLoanById(int loanId) {
        String query = "SELECT * FROM Loans WHERE loan_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, loanId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Loan loan = new Loan();
                    loan.setLoanId(resultSet.getInt("loan_id"));
                    loan.setUserId(resultSet.getInt("user_id"));
                    loan.setBankAccountId(resultSet.getInt("bank_account_id"));
                    loan.setTotalPayment(resultSet.getBigDecimal("total_payment"));
                    loan.setDownPayment(resultSet.getBigDecimal("down_payment"));
                    loan.setInterestRate(resultSet.getBigDecimal("interest_rate"));
                    loan.setTenureYears(resultSet.getInt("tenure_years"));
                    loan.setEmiAmount(resultSet.getBigDecimal("emi_amount"));
                    loan.setSanctionDate(resultSet.getDate("sanction_date"));
                    return loan;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

  
}
