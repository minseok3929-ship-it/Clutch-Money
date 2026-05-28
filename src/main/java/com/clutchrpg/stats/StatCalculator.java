package com.clutchrpg.stats;

import com.clutchrpg.player.PlayerProfile;

public final class StatCalculator {
    public static final double BASE_HEALTH = 100.0;
    public static final double BASE_CRIT_DAMAGE = 1.5;

    private StatCalculator() {}

    public static double effective(int raw) {
        int first = Math.min(raw, 50);
        int second = Math.min(Math.max(raw - 50, 0), 50);
        int third = Math.max(raw - 100, 0);
        return first + second * 0.7 + third * 0.4;
    }

    public static double physicalPower(PlayerProfile profile) {
        return effective(profile.stat(StatType.STR)) * 0.8;
    }

    public static double maxHealth(PlayerProfile profile) {
        return BASE_HEALTH + effective(profile.stat(StatType.STR)) * 2.0 + effective(profile.stat(StatType.VIT)) * 6.0;
    }

    public static double critChance(PlayerProfile profile) {
        return Math.min(0.60, effective(profile.stat(StatType.DEX)) * 0.0015);
    }

    public static double moveSpeedBonus(PlayerProfile profile) {
        return effective(profile.stat(StatType.DEX)) * 0.0005;
    }

    public static double skillDamageBonus(PlayerProfile profile) {
        return effective(profile.stat(StatType.INT)) * 0.01;
    }

    public static double statusDamageBonus(PlayerProfile profile) {
        return effective(profile.stat(StatType.INT)) * 0.005;
    }

    public static double statusResist(PlayerProfile profile) {
        return effective(profile.stat(StatType.VIT)) * 0.001;
    }

    public static double dropRateBonus(PlayerProfile profile) {
        return effective(profile.stat(StatType.LUK)) * 0.001;
    }

    public static double rareDropBonus(PlayerProfile profile) {
        return effective(profile.stat(StatType.LUK)) * 0.0003;
    }

    public static double goldGainBonus(PlayerProfile profile) {
        return effective(profile.stat(StatType.LUK)) * 0.0005;
    }

    public static long expNeeded(int level) {
        if (level >= PlayerProfile.MAX_LEVEL) return Long.MAX_VALUE;
        return Math.round(35 + level * level * 14.0 + level * 18.0);
    }
}
