package com.clutch.rpg.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class MessageUtil {
    public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "CLUTCH RPG" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
    public static final ChatColor ACCENT = ChatColor.AQUA;
    public static final ChatColor RARE = ChatColor.LIGHT_PURPLE;

    private MessageUtil() {
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
}
