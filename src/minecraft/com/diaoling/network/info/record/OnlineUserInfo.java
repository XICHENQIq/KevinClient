package com.diaoling.network.info.record;

import com.diaoling.utils.misc.enums.ClientType;
import com.diaoling.utils.misc.enums.Rank;

public class OnlineUserInfo {
    public final ClientType client;
    public final String username;
    public final String inGameName;
    public final Rank rank;

    public OnlineUserInfo(ClientType client, String username, String inGameName, Rank rank) {
        this.client = client;
        this.username = username;
        this.inGameName = inGameName;
        this.rank = rank;
    }

    // Getters

    public ClientType getClient() {
        return client;
    }

    public String getUsername() {
        return username;
    }

    public String getInGameName() {
        return inGameName;
    }

    public Rank getRank() {
        return rank;
    }
}