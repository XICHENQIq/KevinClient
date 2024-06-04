package com.diaoling.utils.misc.enums;

/**
 * @author DiaoLing
 * @since 4/5/2024
 */
public enum ClientType {
    EMPTY("Empty"),
    EXAMPLE("Example"),
    MOOD("Mood"),
    FOREVER("Forever"),
    KEVIN("Kevin"),
    FASHION("Fashion"),
    SKYRIM("Skyrim"),
    SILENCEFIX("SlienceFix"),
    NAVEN("Naven"),
    TENACITY("Tenacity"),
    RISE("Rise"),
    RAVEN("Raven"),
    AYAKA("Ayaka"),
    APOTHEOSIS("Apotheosis"),
    EMIRIA("Emiria"),
    ASAKA("Asaka"),
    MORAL("Moral"),
    TOMK("Tomk"),
    DRAGON("Dragon"),
    BLOODLINE("BloodLine");

    private final String name;

    ClientType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
