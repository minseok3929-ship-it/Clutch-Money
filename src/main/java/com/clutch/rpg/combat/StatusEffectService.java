package com.clutch.rpg.combat;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class StatusEffectService {
    private final Plugin plugin;
    private final Map<UUID, Map<StatusEffectType, Integer>> activeTasks = new java.util.concurrent.ConcurrentHashMap<>();

    public StatusEffectService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void apply(LivingEntity target, StatusEffectType type, double power, int durationTicks) {
        switch (type) {
            case BLEED, BURN -> dot(target, type, power, durationTicks);
            case SHOCK -> {
                target.damage(power);
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 12, 0.35, 0.45, 0.35, 0.02);
            }
            case FROST -> slow(target, durationTicks);
        }
    }

    private void dot(LivingEntity target, StatusEffectType type, double damage, int durationTicks) {
        cancel(target, type);
        BukkitRunnable runnable = new BukkitRunnable() {
            private int elapsed;
            @Override
            public void run() {
                if (!target.isValid() || target.isDead() || elapsed >= durationTicks) {
                    cancel();
                    return;
                }
                target.damage(damage);
                target.getWorld().spawnParticle(type == StatusEffectType.BURN ? Particle.FLAME : Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 5, 0.25, 0.35, 0.25, 0.01);
                elapsed += 20;
            }
        };
        remember(target, type, runnable.runTaskTimer(plugin, 0L, 20L).getTaskId());
    }

    private void slow(LivingEntity target, int durationTicks) {
        cancel(target, StatusEffectType.FROST);
        double original = target.getAttribute(Attribute.MOVEMENT_SPEED) == null ? 0.1 : target.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
        if (target.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            target.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(Math.max(0.02, original * 0.55));
        }
        target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 20, 0.4, 0.45, 0.4, 0.01);
        int task = new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isValid() && target.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                    target.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(original);
                }
            }
        }.runTaskLater(plugin, durationTicks).getTaskId();
        remember(target, StatusEffectType.FROST, task);
    }

    private void remember(LivingEntity target, StatusEffectType type, int taskId) {
        activeTasks.computeIfAbsent(target.getUniqueId(), ignored -> new EnumMap<>(StatusEffectType.class)).put(type, taskId);
    }

    private void cancel(LivingEntity target, StatusEffectType type) {
        Map<StatusEffectType, Integer> tasks = activeTasks.get(target.getUniqueId());
        if (tasks != null && tasks.containsKey(type)) {
            plugin.getServer().getScheduler().cancelTask(tasks.remove(type));
        }
    }
}
