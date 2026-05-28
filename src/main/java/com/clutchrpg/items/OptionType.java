package com.clutchrpg.items;

public enum OptionType {
    ATTACK_DAMAGE("공격력"), SKILL_DAMAGE("스킬 피해"), CRIT_CHANCE("치명타 확률"), CRIT_DAMAGE("치명타 피해"),
    ARMOR_PENETRATION("방어 관통"), MAX_HEALTH("최대 체력"), DAMAGE_REDUCTION("피해 감소"), MOVE_SPEED("이동속도"),
    DASH_COOLDOWN_REDUCTION("대쉬 쿨타임 감소"), DROP_RATE("드랍률"), GOLD_GAIN("골드 획득량"),
    BLEED_DAMAGE("출혈 피해"), SHOCK_DAMAGE("감전 피해"), BURN_DAMAGE("화상 피해"), FROST_DURATION("냉기 지속시간");

    private final String korean;
    OptionType(String korean) { this.korean = korean; }
    public String korean() { return korean; }
}
