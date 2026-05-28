package com.clutch.rpg.commands;

import com.clutch.rpg.bosses.BossManager;
import com.clutch.rpg.items.CustomItemFactory;
import com.clutch.rpg.items.Rarity;
import com.clutch.rpg.items.WeaponType;
import com.clutch.rpg.mobs.MobManager;
import com.clutch.rpg.player.PlayerData;
import com.clutch.rpg.player.PlayerDataManager;
import com.clutch.rpg.stats.StatType;
import com.clutch.rpg.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ClutchRPGCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;
    private final CustomItemFactory itemFactory;
    private final MobManager mobManager;
    private final BossManager bossManager;

    public ClutchRPGCommand(Plugin plugin, PlayerDataManager playerDataManager, CustomItemFactory itemFactory, MobManager mobManager, BossManager bossManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.itemFactory = itemFactory;
        this.mobManager = mobManager;
        this.bossManager = bossManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, "플레이어만 사용할 수 있습니다.");
            return true;
        }
        if (args.length == 0) {
            help(player);
            return true;
        }
        try {
            route(player, args);
        } catch (IllegalArgumentException exception) {
            MessageUtil.send(player, "&c잘못된 인자입니다: &7" + exception.getMessage());
        }
        return true;
    }

    private void route(Player player, String[] args) {
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "stats" -> stats(player);
            case "stat" -> {
                require(args, 4);
                if (!"add".equalsIgnoreCase(args[1])) {
                    throw new IllegalArgumentException("/crpg stat add <stat> <amount>");
                }
                StatType type = StatType.valueOf(args[2].toUpperCase(Locale.ROOT));
                int amount = Integer.parseInt(args[3]);
                playerDataManager.get(player).stats().add(type, amount);
                playerDataManager.applyDerivedAttributes(player);
                MessageUtil.send(player, "&b" + type + " &7+" + amount);
            }
            case "level" -> {
                require(args, 3);
                if (!"set".equalsIgnoreCase(args[1])) {
                    throw new IllegalArgumentException("/crpg level set <level>");
                }
                int level = Integer.parseInt(args[2]);
                playerDataManager.get(player).setLevel(level);
                MessageUtil.send(player, "&7레벨 설정: &d" + level);
            }
            case "item" -> {
                require(args, 4);
                if (!"give".equalsIgnoreCase(args[1])) {
                    throw new IllegalArgumentException("/crpg item give <weaponType> <rarity>");
                }
                WeaponType weaponType = WeaponType.valueOf(args[2].toUpperCase(Locale.ROOT));
                Rarity rarity = Rarity.valueOf(args[3].toUpperCase(Locale.ROOT));
                player.getInventory().addItem(itemFactory.createWeapon(weaponType, rarity, Math.max(1, playerDataManager.get(player).level() / 2)));
                MessageUtil.send(player, rarity.color() + rarity.name() + " &7" + weaponType + " 지급");
            }
            case "mob" -> {
                require(args, 3);
                if (!"spawn".equalsIgnoreCase(args[1])) {
                    throw new IllegalArgumentException("/crpg mob spawn <mobId>");
                }
                mobManager.spawn(mobManager.parse(args[2]), player.getLocation());
                MessageUtil.send(player, "&7몬스터 소환: &b" + args[2]);
            }
            case "boss" -> {
                require(args, 3);
                if (!"spawn".equalsIgnoreCase(args[1]) || !BossManager.FOREST_GUARDIAN.equalsIgnoreCase(args[2])) {
                    throw new IllegalArgumentException("/crpg boss spawn forest_guardian");
                }
                bossManager.spawnForestGuardian(player.getLocation());
                MessageUtil.send(player, "&d숲의 수호자 &7소환");
            }
            case "reload" -> {
                plugin.reloadConfig();
                MessageUtil.send(player, "&7프로토타입 설정 리로드 완료");
            }
            default -> help(player);
        }
    }

    private void stats(Player player) {
        PlayerData data = playerDataManager.get(player);
        MessageUtil.send(player, "&7Lv.&d" + data.level() + " &7EXP &b" + data.exp() + "&8/&b" + playerDataManager.expToNext(data.level()) + " &7포인트 &d" + data.statPoints());
        MessageUtil.send(player, "&7STR &b" + data.stats().get(StatType.STR) + " &7DEX &b" + data.stats().get(StatType.DEX) + " &7INT &b" + data.stats().get(StatType.INT) + " &7VIT &b" + data.stats().get(StatType.VIT) + " &7LUK &b" + data.stats().get(StatType.LUK));
    }

    private void help(Player player) {
        MessageUtil.send(player, "&7/crpg stats, stat add, level set, item give, mob spawn, boss spawn, reload");
    }

    private void require(String[] args, int length) {
        if (args.length < length) {
            throw new IllegalArgumentException("인자가 부족합니다.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("stats", "stat", "level", "item", "mob", "boss", "reload");
        }
        if (args.length == 3 && "item".equalsIgnoreCase(args[0])) {
            return Arrays.stream(WeaponType.values()).map(Enum::name).map(String::toLowerCase).toList();
        }
        if (args.length == 4 && "item".equalsIgnoreCase(args[0])) {
            return Arrays.stream(Rarity.values()).map(Enum::name).map(String::toLowerCase).toList();
        }
        if (args.length == 3 && "stat".equalsIgnoreCase(args[0])) {
            return Arrays.stream(StatType.values()).map(Enum::name).map(String::toLowerCase).toList();
        }
        return List.of();
    }
}
