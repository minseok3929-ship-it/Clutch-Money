package com.clutchrpg.combat;

public enum StatusEffectType {
    BLEED("출혈"), SHOCK("감전"), BURN("화상"), FROST("냉기");
    private final String korean;
    StatusEffectType(String korean) { this.korean = korean; }
    public String korean() { return korean; }
}
