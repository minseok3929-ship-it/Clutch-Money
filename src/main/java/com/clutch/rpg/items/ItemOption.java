package com.clutch.rpg.items;

public record ItemOption(OptionType type, double value) {
    public String serialize() {
        return type.name() + ":" + value;
    }

    public static ItemOption parse(String raw) {
        String[] split = raw.split(":", 2);
        return new ItemOption(OptionType.valueOf(split[0]), Double.parseDouble(split[1]));
    }
}
