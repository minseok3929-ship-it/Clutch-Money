package com.clutchrpg.mobs;

import com.clutchrpg.util.Keys;
import com.clutchrpg.util.ParticleEffects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class MobManager {
    private final Keys keys;

    public MobManager(Keys keys) { this.keys = keys; }

    public LivingEntity spawn(ForestMobType type, Location location) {
        return spawn(type, location, -1);
    }

    public LivingEntity spawn(ForestMobType type, Location location, int spawnPointId) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type.entityType());
        entity.getPersistentDataContainer().set(keys.mobId, PersistentDataType.STRING, type.id());
        if (spawnPointId >= 0) entity.getPersistentDataContainer().set(keys.spawnPointId, PersistentDataType.INTEGER, spawnPointId);
        entity.setRemoveWhenFarAway(false);
        applyStats(entity, type);
        applyVanillaControl(entity, type);
        updateName(entity);
        spawnPresentation(entity, type);
        return entity;
    }

    private void applyStats(LivingEntity entity, ForestMobType type) {
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(type.health());
        if (entity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            double speed = switch (type) {
                case FOREST_SLIME -> 0.34;
                case FOREST_WOLF -> 0.38;
                case GOBLIN -> 0.30;
                case VINE_GOLEM -> 0.18;
            };
            entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
        }
        if (entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE) != null && type == ForestMobType.VINE_GOLEM) {
            entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(0.82);
        }
        entity.setHealth(type.health());
    }

    private void applyVanillaControl(LivingEntity entity, ForestMobType type) {
        if (entity instanceof Slime slime) {
            slime.setSize(1);
        }
        if (entity instanceof Zombie zombie) {
            zombie.setShouldBurnInDay(false);
            zombie.getEquipment().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.WOODEN_SWORD));
            zombie.getEquipment().setItemInOffHand(new ItemStack(Material.OAK_SAPLING));
            zombie.getEquipment().setHelmetDropChance(0f);
            zombie.getEquipment().setItemInMainHandDropChance(0f);
            zombie.getEquipment().setItemInOffHandDropChance(0f);
        }
        if (entity instanceof Wolf wolf) {
            wolf.setAngry(true);
            wolf.setCollarColor(org.bukkit.DyeColor.GREEN);
        }
        if (entity instanceof IronGolem golem) {
            golem.setPlayerCreated(false);
        }
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
            case FOREST_SLIME -> entity.getWorld().spawnParticle(Particle.DUST, loc, 38, 0.5, 0.45, 0.5, 0, ParticleEffects.GREEN_DUST);
            case FOREST_WOLF -> {
                entity.getWorld().spawnParticle(Particle.CLOUD, loc, 18, 0.45, 0.2, 0.45, 0.04);
                entity.getWorld().spawnParticle(Particle.DUST, loc, 22, 0.45, 0.25, 0.45, 0, ParticleEffects.DARK_GREEN_DUST);
            }
            case GOBLIN -> {
                entity.getWorld().spawnParticle(Particle.CRIT, loc, 18, 0.45, 0.45, 0.45, 0.04);
                entity.getWorld().spawnParticle(Particle.DUST, loc, 24, 0.45, 0.45, 0.45, 0, ParticleEffects.WOOD_DUST);
            }
            case VINE_GOLEM -> entity.getWorld().spawnParticle(Particle.DUST_PLUME, entity.getLocation(), 50, 0.65, 0.2, 0.65, 0.05);
        }
        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GRASS_BREAK, 0.7f, 0.8f + type.level() * 0.04f);
    }
}
