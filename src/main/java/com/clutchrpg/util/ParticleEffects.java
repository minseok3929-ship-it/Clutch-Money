package com.clutchrpg.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class ParticleEffects {
    public static final Particle.DustOptions CYAN_DUST = new Particle.DustOptions(Color.fromRGB(0x21, 0xF6, 0xE5), 1.35f);
    public static final Particle.DustOptions PURPLE_DUST = new Particle.DustOptions(Color.fromRGB(0xB4, 0x5C, 0xFF), 1.55f);
    public static final Particle.DustOptions GREEN_DUST = new Particle.DustOptions(Color.fromRGB(0x45, 0xD9, 0x55), 1.35f);
    public static final Particle.DustOptions DARK_GREEN_DUST = new Particle.DustOptions(Color.fromRGB(0x1F, 0x8A, 0x3B), 1.5f);
    public static final Particle.DustOptions RED_DUST = new Particle.DustOptions(Color.fromRGB(0xE8, 0x3D, 0x32), 1.45f);
    public static final Particle.DustOptions BLUE_DUST = new Particle.DustOptions(Color.fromRGB(0x3A, 0x7D, 0xFF), 1.4f);
    public static final Particle.DustOptions WOOD_DUST = new Particle.DustOptions(Color.fromRGB(0x7A, 0x4E, 0x2D), 1.25f);
    public static final Particle.DustOptions SILVER_DUST = new Particle.DustOptions(Color.fromRGB(0xD9, 0xE2, 0xE8), 1.25f);
    public static final Particle.DustOptions GOLD_DUST = new Particle.DustOptions(Color.fromRGB(0xF2, 0xC9, 0x45), 1.35f);

    private ParticleEffects() {}

    public static void slashArc(Location origin, Vector direction, double radius, double degrees, Particle.DustOptions dust, int points) {
        World world = origin.getWorld();
        Vector forward = direction.clone().setY(0).normalize();
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();
        double half = Math.toRadians(degrees / 2.0);
        for (int i = 0; i <= points; i++) {
            double t = -half + (half * 2.0 * i / points);
            Vector point = forward.clone().multiply(Math.cos(t) * radius).add(right.clone().multiply(Math.sin(t) * radius));
            Location loc = origin.clone().add(point).add(0, 0.9 + Math.sin((double) i / points * Math.PI) * 0.25, 0);
            world.spawnParticle(Particle.DUST, loc, 2, 0.025, 0.025, 0.025, 0, dust);
        }
    }


    public static void verticalSlash(Location origin, Vector direction, double length, double height, Particle.DustOptions dust, int points) {
        World world = origin.getWorld();
        Vector forward = direction.clone().setY(0).normalize();
        Location top = origin.clone().add(forward.clone().multiply(length * 0.45)).add(0, height, 0);
        Location bottom = origin.clone().add(forward.clone().multiply(length)).add(0, 0.25, 0);
        Vector delta = bottom.toVector().subtract(top.toVector());
        for (int i = 0; i <= points; i++) {
            Location loc = top.clone().add(delta.clone().multiply((double) i / points));
            world.spawnParticle(Particle.DUST, loc, 3, 0.03, 0.03, 0.03, 0, dust);
            if (i % 3 == 0) world.spawnParticle(Particle.CRIT, loc, 1, 0.02, 0.02, 0.02, 0.02);
        }
    }

    public static void expandingRing(Location center, double maxRadius, Particle.DustOptions dust, int rings, long stepTicks, org.bukkit.plugin.Plugin plugin) {
        for (int i = 1; i <= rings; i++) {
            final double radius = maxRadius * i / rings;
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> ring(center, radius, Particle.DUST, dust, 24 + (int) (radius * 8)), i * stepTicks);
        }
    }

    public static void ring(Location center, double radius, Particle particle, Object data, int points) {
        World world = center.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0 * i / points;
            Location loc = center.clone().add(Math.cos(angle) * radius, 0.05, Math.sin(angle) * radius);
            if (data == null) world.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
            else world.spawnParticle(particle, loc, 1, 0, 0, 0, 0, data);
        }
    }

    public static void line(Location from, Location to, Particle.DustOptions dust, int points) {
        World world = from.getWorld();
        Vector delta = to.toVector().subtract(from.toVector());
        for (int i = 0; i <= points; i++) {
            Location loc = from.clone().add(delta.clone().multiply((double) i / points));
            world.spawnParticle(Particle.DUST, loc, 1, 0.015, 0.015, 0.015, 0, dust);
        }
    }
}
