package com.clutchrpg.stats;

public enum StatType {
    STR("힘"), DEX("민첩"), INT("지력"), VIT("체력"), LUK("행운");

    private final String korean;

    StatType(String korean) { this.korean = korean; }

    public String korean() { return korean; }
}
