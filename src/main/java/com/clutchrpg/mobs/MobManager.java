package com.clutchrpg.mobs;

import com.clutchrpg.util.Keys;
import com.clutchrpg.util.ParticleEffects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

public final class MobManager {
    private final Keys keys;

    public MobManager(Keys keys) { this.keys = keys; }

    public LivingEntity spawn(ForestMobType type, Location location) {
        return spawn(type, location, -1);
    }

    public LivingEntity spawn(ForestMobType type, Location location, int spawnPointId) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type.entityType());
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(type.health());
        entity.setHealth(type.health());
        entity.getPersistentDataContainer().set(keys.mobId, PersistentDataType.STRING, type.id());
        if (spawnPointId >= 0) entity.getPersistentDataContainer().set(keys.spawnPointId, PersistentDataType.INTEGER, spawnPointId);
        entity.setRemoveWhenFarAway(false);
        updateName(entity);
        spawnPresentation(entity, type);
        return entity;
    }

    public void updateName(LivingEntity entity) {
        ForestMobType type = typeOf(entity);
        if (type == null) return;
        int hp = (int) Math.ceil(Math.max(0, entity.getHealth()));
        int max = (int) Math.ceil(entity.getAttribute(Attribute.MAX_HEALTH).getValue());
        entity.customName(Component.text("Lv." + type.level() + " " + type.koreanName() + "  ❤ " + hp + "/" + max, NamedTextColor.GREEN));
        entity.setCustomNameVisible(true);
    }

    public ForestMobType typeOf(LivingEntity entity) {
        String id = entity.getPersistentDataContainer().get(keys.mobId, PersistentDataType.STRING);
        return id == null ? null : ForestMobType.byId(id);
    }

    public int spawnPointId(LivingEntity entity) {
        Integer id = entity.getPersistentDataContainer().get(keys.spawnPointId, PersistentDataType.INTEGER);
        return id == null ? -1 : id;
    }

    private void spawnPresentation(LivingEntity entity, ForestMobType type) {
        Location loc = entity.getLocation().add(0, 0.8, 0);
        switch (type) {
            case FOREST_SLIME -> entity.getWorld().spawnParticle(Particle.DUST, loc, 35, 0.5, 0.45, 0.5, 0, ParticleEffects.GREEN_DUST);
            case FOREST_WOLF -> entity.getWorld().spawnParticle(Particle.CLOUD, loc, 24, 0.5, 0.25, 0.5, 0.04);
            case GOBLIN -> entity.getWorld().spawnParticle(Particle.CRIT, loc, 24, 0.45, 0.45, 0.45, 0.04);
            case VINE_GOLEM -> entity.getWorld().spawnParticle(Particle.DUST_PLUME, entity.getLocation(), 45, 0.65, 0.2, 0.65, 0.05);
        }
        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GRASS_BREAK, 0.7f, 0.8f + type.level() * 0.04f);
    }
}
