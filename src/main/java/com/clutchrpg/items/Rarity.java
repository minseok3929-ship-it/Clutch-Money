package com.clutchrpg.items;

import net.kyori.adventure.text.format.TextColor;

public enum Rarity {
    COMMON("일반", TextColor.color(0xBFC7C7), 1),
    RARE("희귀", TextColor.color(0x21F6E5), 2),
    EPIC("영웅", TextColor.color(0xB45CFF), 3),
    LEGENDARY("전설", TextColor.color(0xFFB020), 4),
    MYTHIC("신화", TextColor.color(0xFF3D8B), 5);

    private final String korean;
    private final TextColor color;
    private final int optionSlots;
    Rarity(String korean, TextColor color, int optionSlots) { this.korean = korean; this.color = color; this.optionSlots = optionSlots; }
    public String korean() { return korean; }
    public TextColor color() { return color; }
    public int optionSlots() { return optionSlots; }
}
