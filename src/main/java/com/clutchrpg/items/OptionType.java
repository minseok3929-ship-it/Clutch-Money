package com.clutchrpg.items;

public enum OptionType {
    ATTACK_DAMAGE("공격력", false), SKILL_DAMAGE("스킬 피해", true), CRIT_CHANCE("치명타 확률", true), CRIT_DAMAGE("치명타 피해", true),
    ARMOR_PENETRATION("방어 관통", true), MAX_HEALTH("최대 체력", false), DAMAGE_REDUCTION("피해 감소", true), MOVE_SPEED("이동속도", true),
    DASH_COOLDOWN_REDUCTION("대쉬 쿨타임 감소", true), DROP_RATE("드랍률", true), GOLD_GAIN("골드 획득량", true),
    BLEED_DAMAGE("출혈 피해", true), SHOCK_DAMAGE("감전 피해", true), BURN_DAMAGE("화상 피해", true), FROST_DURATION("냉기 지속시간", false);

    private final String korean;
    private final boolean percent;
    OptionType(String korean, boolean percent) { this.korean = korean; this.percent = percent; }
    public String korean() { return korean; }
    public boolean percent() { return percent; }
}
