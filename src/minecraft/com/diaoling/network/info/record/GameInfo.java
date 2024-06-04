package com.diaoling.network.info.record;

public class GameInfo {
    private final String inGameName;
    private final String token;
    private final String uuid;
    private final long lastUpdateTime;

    public GameInfo(String inGameName, String token, String uuid, long lastUpdateTime) {
        this.inGameName = inGameName;
        this.token = token;
        this.uuid = uuid;
        this.lastUpdateTime = lastUpdateTime;
    }

    // Getters

    public String getInGameName() {
        return inGameName;
    }

    public String getToken() {
        return token;
    }

    public String getUuid() {
        return uuid;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}