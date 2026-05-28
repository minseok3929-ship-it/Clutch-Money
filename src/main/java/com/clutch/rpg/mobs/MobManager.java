package com.clutch.rpg.mobs;

import com.clutch.rpg.items.CustomItemFactory;
import com.clutch.rpg.items.Rarity;
import com.clutch.rpg.items.WeaponType;
import com.clutch.rpg.player.PlayerDataManager;
import com.clutch.rpg.util.Keys;
import com.clutch.rpg.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;
import java.util.Random;

public class MobManager implements Listener {
    private final Plugin plugin;
    private final Keys keys;
    private final PlayerDataManager playerDataManager;
    private final CustomItemFactory itemFactory;
    private final Random random = new Random();

    public MobManager(Plugin plugin, Keys keys, PlayerDataManager playerDataManager, CustomItemFactory itemFactory) {
        this.plugin = plugin;
        this.keys = keys;
        this.playerDataManager = playerDataManager;
        this.itemFactory = itemFactory;
    }

    public LivingEntity spawn(ForestMobType type, Location location) {
        Entity entity = location.getWorld().spawnEntity(location, type.entityType());
        LivingEntity living = (LivingEntity) entity;
        living.setCustomName(type.displayName());
        living.setCustomNameVisible(true);
        if (living.getAttribute(Attribute.MAX_HEALTH) != null) {
            living.getAttribute(Attribute.MAX_HEALTH).setBaseValue(type.health());
        }
        living.setHealth(type.health());
        living.getPersistentDataContainer().set(keys.mobId, PersistentDataType.STRING, type.name());
        return living;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        String mobId = event.getEntity().getPersistentDataContainer().get(keys.mobId, PersistentDataType.STRING);
        if (mobId == null) {
            return;
        }
        event.getDrops().clear();
        ForestMobType type = ForestMobType.valueOf(mobId);
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            playerDataManager.addExp(killer, type.exp());
            MessageUtil.send(killer, "&7경험치 획득: &b+" + type.exp());
            if (random.nextDouble() < 0.35) {
                giveDrop(killer, type);
            }
        }
        Location respawn = event.getEntity().getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                spawn(type, respawn);
            }
        }.runTaskLater(plugin, 80L);
    }

    private void giveDrop(Player player, ForestMobType mobType) {
        Rarity rarity = rollRarity();
        WeaponType weaponType = WeaponType.values()[random.nextInt(3)];
        ItemStack item = itemFactory.createWeapon(weaponType, rarity, Math.max(1, mobType.level() / 2));
        if (player.getInventory().firstEmpty() >= 0) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        if (rarity.ordinal() >= Rarity.RARE.ordinal()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.1f);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 45, 0.6, 0.8, 0.6, 0.04);
            MessageUtil.send(player, rarity.color() + rarity.name() + " &7장비 획득!");
        }
    }

    private Rarity rollRarity() {
        double value = random.nextDouble();
        if (value < 0.08) {
            return Rarity.EPIC;
        }
        if (value < 0.28) {
            return Rarity.RARE;
        }
        return Rarity.COMMON;
    }

    public ForestMobType parse(String raw) {
        return ForestMobType.valueOf(raw.toUpperCase(Locale.ROOT));
    }
}
