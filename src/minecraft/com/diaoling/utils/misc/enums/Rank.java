package com.diaoling.utils.misc.enums;

import net.minecraft.util.text.TextFormatting;

public enum Rank {
    EMPTY("Empty"),
    USER("User"),
    CONTRIBUTOR("Contributor"),
    BETA("Beta"),
    TESTER("Tester"),
    MEDIA("Media"),
    PARTNER("Partner"),
    MODERATOR("Moderator"),
    DEV("Dev"),
    ADMIN("Admin"),
    OWNER("Owner");

    private final String name;

    Rank(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public TextFormatting color() {
        return color(this);
    }

    public TextFormatting color(String rank) {
        return color(Rank.valueOf(rank.toUpperCase()));
    }

    public TextFormatting color(Rank rank) {
        if (rank == null) {
            rank = Rank.EMPTY;
        }
        if (rank == Rank.OWNER) {
            return TextFormatting.RED;
        } else if (rank == Rank.ADMIN) {
            return TextFormatting.GOLD;
        } else if (rank == Rank.DEV) {
            return TextFormatting.LIGHT_PURPLE;
        } else if (rank == Rank.MODERATOR) {
            return TextFormatting.YELLOW;
        } else if (rank == Rank.PARTNER) {
            return TextFormatting.GREEN;
        } else if (rank == Rank.MEDIA) {
            return TextFormatting.DARK_PURPLE;
        } else if (rank == Rank.CONTRIBUTOR) {
            return TextFormatting.AQUA;
        } else if (rank == Rank.BETA) {
            return TextFormatting.BLUE;
        } else if (rank == Rank.TESTER) {
            return TextFormatting.DARK_BLUE;
        } else if (rank == Rank.USER) {
            return TextFormatting.GRAY;
        } else {
            return TextFormatting.WHITE;
        }
    }
}