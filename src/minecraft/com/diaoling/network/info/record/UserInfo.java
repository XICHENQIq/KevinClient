package com.diaoling.network.info.record;

import com.diaoling.utils.misc.enums.Rank;

import java.util.Date;

public class UserInfo {
    private final String token;
    private final int userId;
    private final String username;
    private final Rank rank;
    private final Date expiryDate;
    private final double balance;

    public UserInfo(String token, int userId, String username, Rank rank, Date expiryDate, double balance) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.rank = rank;
        this.expiryDate = expiryDate;
        this.balance = balance;
    }

    // Getters

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Rank getRank() {
        return rank;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public double getBalance() {
        return balance;
    }
}