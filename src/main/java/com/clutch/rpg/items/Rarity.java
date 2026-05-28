package com.clutch.rpg.items;

import org.bukkit.ChatColor;

public enum Rarity {
    COMMON(1, ChatColor.GRAY),
    RARE(2, ChatColor.AQUA),
    EPIC(3, ChatColor.LIGHT_PURPLE),
    LEGENDARY(4, ChatColor.GOLD),
    MYTHIC(5, ChatColor.DARK_PURPLE);

    private final int optionCount;
    private final ChatColor color;

    Rarity(int optionCount, ChatColor color) {
        this.optionCount = optionCount;
        this.color = color;
    }

    public int optionCount() { return optionCount; }
    public ChatColor color() { return color; }
    public boolean hasAffixes() { return this.ordinal() >= EPIC.ordinal(); }
    public boolean hasUniqueEffect() { return this.ordinal() >= LEGENDARY.ordinal(); }
    public boolean canChangePlayStyle() { return this == MYTHIC; }
}
