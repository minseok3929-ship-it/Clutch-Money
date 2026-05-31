package com.clutchrpg.skills;

import com.clutchrpg.items.WeaponType;

public abstract class AbstractWeaponSkill implements WeaponSkill {
    private final String id;
    private final String koreanName;
    private final WeaponType weaponType;
    private final long cooldownMillis;

    protected AbstractWeaponSkill(String id, String koreanName, WeaponType weaponType, long cooldownMillis) {
        this.id = id; this.koreanName = koreanName; this.weaponType = weaponType; this.cooldownMillis = cooldownMillis;
    }
    public String id() { return id; }
    public String koreanName() { return koreanName; }
    public WeaponType weaponType() { return weaponType; }
    public long cooldownMillis() { return cooldownMillis; }
}
