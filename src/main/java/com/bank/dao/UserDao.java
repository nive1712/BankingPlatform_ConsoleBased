package com.bank.dao;

import com.bank.model.User;
import java.math.BigDecimal;
import java.util.List;

public interface UserDao {
    int saveUser(User user);
    User findUserByEmail(String email);
    List<User> getAllUsers();
    User getUserByEmail(String email);
    boolean withdraw(User user, BigDecimal amount);
    boolean deposit(User user, BigDecimal amount);
    boolean transfer(User fromUser, User toUser, BigDecimal amount);
	  User getUserById(int userId);
	
}

