package com.diaoling.utils.misc.enums;

/**
 * @author DiaoLing
 * @since 4/9/2024
 */
public enum EventState {
    PRE("Pre"),
    POST("Post");

    private final String name;

    EventState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
