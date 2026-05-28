package com.clutchrpg.mobs;

import com.clutchrpg.util.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

public final class MobManager {
    private final Keys keys;

    public MobManager(Keys keys) { this.keys = keys; }

    public LivingEntity spawn(ForestMobType type, Location location) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type.entityType());
        entity.customName(Component.text("Lv." + type.level() + " " + type.koreanName(), NamedTextColor.GREEN));
        entity.setCustomNameVisible(true);
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(type.health());
        entity.setHealth(type.health());
        entity.getPersistentDataContainer().set(keys.mobId, PersistentDataType.STRING, type.id());
        entity.setRemoveWhenFarAway(false);
        return entity;
    }

    public ForestMobType typeOf(LivingEntity entity) {
        String id = entity.getPersistentDataContainer().get(keys.mobId, PersistentDataType.STRING);
        return id == null ? null : ForestMobType.byId(id);
    }
}
