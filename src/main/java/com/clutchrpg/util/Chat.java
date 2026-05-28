package com.clutchrpg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

public final class Chat {
    public static final TextColor CYAN = TextColor.color(0x21F6E5);
    public static final TextColor PURPLE = TextColor.color(0xB45CFF);
    public static final TextColor GRAY = TextColor.color(0x9CA3AF);
    public static final Component PREFIX = Component.text("[", NamedTextColor.DARK_GRAY)
            .append(Component.text("CLUTCH RPG", CYAN))
            .append(Component.text("] ", NamedTextColor.DARK_GRAY));

    private Chat() {}

    public static Component text(String message) {
        return Component.text(message, GRAY);
    }

    public static Component accent(String message) {
        return Component.text(message, CYAN);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(PREFIX.append(text(message)));
    }

    public static void send(CommandSender sender, Component component) {
        sender.sendMessage(PREFIX.append(component));
    }
}
