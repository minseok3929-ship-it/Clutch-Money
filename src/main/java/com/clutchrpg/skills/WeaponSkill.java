package com.clutchrpg.skills;

import com.clutchrpg.items.WeaponType;
import org.bukkit.entity.Player;

public interface WeaponSkill {
    String id();
    String koreanName();
    WeaponType weaponType();
    long cooldownMillis();
    void cast(Player player);
}
