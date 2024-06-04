package com.diaoling.utils.misc.enums;

/**
 * @author DiaoLing
 * @since 4/11/2024
 */
public enum BuildType {
    ALPHA("Alpha"),
    Beta("Beta"),
    DEBUG("Debug"),
    DEVELOPER("Developer"),
    RELEASE("Release");

    private final String name;

    BuildType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
