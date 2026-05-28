package com.clutchrpg.skills;

import com.clutchrpg.items.WeaponType;
import java.util.*;

public final class SkillManager {
    private final Map<WeaponType, List<WeaponSkill>> rightClickSkills = new EnumMap<>(WeaponType.class);
    private final Map<String, WeaponSkill> keySlots = new HashMap<>();

    public void registerRightClick(WeaponSkill skill) {
        rightClickSkills.computeIfAbsent(skill.weaponType(), ignored -> new ArrayList<>()).add(skill);
    }

    public List<WeaponSkill> rightClickSkills(WeaponType type) {
        return List.copyOf(rightClickSkills.getOrDefault(type, List.of()));
    }

    public void bindKeySlot(String slot, WeaponSkill skill) {
        keySlots.put(slot.toUpperCase(Locale.ROOT), skill);
    }

    public Optional<WeaponSkill> keySlot(String slot) {
        return Optional.ofNullable(keySlots.get(slot.toUpperCase(Locale.ROOT)));
    }
}
