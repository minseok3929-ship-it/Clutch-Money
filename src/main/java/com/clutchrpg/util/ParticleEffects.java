package com.clutchrpg.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class ParticleEffects {
    public static final Particle.DustOptions CYAN_DUST = new Particle.DustOptions(Color.fromRGB(0x21, 0xF6, 0xE5), 1.35f);
    public static final Particle.DustOptions PURPLE_DUST = new Particle.DustOptions(Color.fromRGB(0xB4, 0x5C, 0xFF), 1.55f);
    public static final Particle.DustOptions GREEN_DUST = new Particle.DustOptions(Color.fromRGB(0x5C, 0xFF, 0x76), 1.35f);
    public static final Particle.DustOptions RED_DUST = new Particle.DustOptions(Color.fromRGB(0xFF, 0x45, 0x45), 1.45f);

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
