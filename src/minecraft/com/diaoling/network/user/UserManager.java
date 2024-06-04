package com.diaoling.network.user;

import com.diaoling.network.info.record.OnlineUserInfo;
import com.yumegod.obfuscation.Native;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DiaoLing
 * @since 4/10/2024
 */
@Native
public class UserManager {
    private User user;
    private List<OnlineUserInfo> onlineUsers = new ArrayList<>();

    /**
     * Initializes the UserManager.
     */
    /*@Override
    public void initialize() {
        Exampel.getEventBus().register(this);
    }*/

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean add(OnlineUserInfo onlineUserInfo) {
        return onlineUsers.add(onlineUserInfo);
    }

    public boolean remove(OnlineUserInfo onlineUserInfo) {
        return onlineUsers.remove(onlineUserInfo);
    }

    public List<OnlineUserInfo> getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(List<OnlineUserInfo> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public List<String> getUsernames() {
        return onlineUsers.stream()
                .map(OnlineUserInfo::getUsername)
                .collect(Collectors.toList());
    }
}
