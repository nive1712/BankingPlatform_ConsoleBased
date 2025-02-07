package com.bank.model;

public class NetBanking {
    private int netBankingId;
    private int userId;
    private String password;

    public int getNetBankingId() {
        return netBankingId;
    }

    public void setNetBankingId(int netBankingId) {
        this.netBankingId = netBankingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
