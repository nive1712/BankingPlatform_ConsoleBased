package com.bank.dao;

import com.bank.model.*;

public interface NetBankingDao {
    void saveNetBanking(NetBanking netBanking);
    NetBanking getNetBankingByUserId(int userId);
}

