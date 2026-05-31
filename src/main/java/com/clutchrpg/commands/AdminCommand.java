package com.clutchrpg.commands;

import com.clutchrpg.bosses.ForestGuardianBoss;
import com.clutchrpg.items.DropService;
import com.clutchrpg.items.ItemFactory;
import com.clutchrpg.items.Rarity;
import com.clutchrpg.items.WeaponType;
import com.clutchrpg.mobs.ForestMobType;
import com.clutchrpg.mobs.MobManager;
import com.clutchrpg.mobs.SpawnPoint;
import com.clutchrpg.mobs.SpawnPointManager;
import com.clutchrpg.util.Chat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class AdminCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    private final MobManager mobManager;
    private final SpawnPointManager spawnPointManager;
    private final ForestGuardianBoss boss;
    private final ItemFactory itemFactory;
    private final DropService dropService;

    public AdminCommand(Plugin plugin, MobManager mobManager, SpawnPointManager spawnPointManager, ForestGuardianBoss boss, ItemFactory itemFactory, DropService dropService) {
        this.plugin = plugin; this.mobManager = mobManager; this.spawnPointManager = spawnPointManager; this.boss = boss; this.itemFactory = itemFactory; this.dropService = dropService;
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clutchrpg.admin")) {
            Chat.send(sender, "권한이 없습니다.");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            mobManager.reloadDefinitions();
            spawnPointManager.reload();
            Chat.send(sender, "설정과 스폰 포인트를 다시 불러왔습니다.");
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("spawnpoint") && args[1].equalsIgnoreCase("list")) {
            spawnPointManager.sendList(sender);
            return true;
        }
        if (!(sender instanceof Player player)) {
            Chat.send(sender, "인게임 관리자만 스폰/지급/스폰포인트 편집 명령어를 사용할 수 있습니다.");
            return true;
        }
        if (args.length >= 5 && args[0].equalsIgnoreCase("spawnpoint") && args[1].equalsIgnoreCase("add")) {
            ForestMobType type = ForestMobType.byId(args[2]);
            if (type == null) { Chat.send(player, "알 수 없는 몬스터 ID입니다."); return true; }
            try {
                int maxAlive = Integer.parseInt(args[3]);
                double radius = Double.parseDouble(args[4]);
                SpawnPoint point = spawnPointManager.add(type, player.getLocation(), maxAlive, radius);
                Chat.send(player, "스폰 캠프 #" + point.id() + " 등록: " + type.koreanName() + " / 최대 " + point.safeMaxAlive() + " / 반경 " + (int) point.safeRadius());
            } catch (NumberFormatException ex) {
                Chat.send(player, "사용법: /crpg spawnpoint add <mobId> <maxAlive> <radius>");
            }
            return true;
        }
        if (args.length >= 3 && args[0].equalsIgnoreCase("spawnpoint") && args[1].equalsIgnoreCase("remove")) {
            try {
                int id = Integer.parseInt(args[2]);
                Chat.send(player, spawnPointManager.remove(id) ? "스폰 포인트 #" + id + " 제거 완료." : "해당 ID의 스폰 포인트가 없습니다.");
            } catch (NumberFormatException ex) {
                Chat.send(player, "사용법: /crpg spawnpoint remove <id>");
            }
            return true;
        }
        if (args.length >= 3 && args[0].equalsIgnoreCase("mob") && args[1].equalsIgnoreCase("spawn")) {
            ForestMobType type = ForestMobType.byId(args[2]);
            if (type == null) { Chat.send(player, "알 수 없는 몬스터 ID입니다."); return true; }
            mobManager.spawn(type, player.getLocation());
            Chat.send(player, type.koreanName() + " 테스트 스폰 완료.");
            return true;
        }
        if (args.length >= 3 && args[0].equalsIgnoreCase("boss") && args[1].equalsIgnoreCase("spawn") && args[2].equalsIgnoreCase(ForestGuardianBoss.ID)) {
            boss.spawn(player.getLocation());
            Chat.send(player, "숲의 수호자 스폰 완료.");
            return true;
        }
        if (args.length >= 4 && args[0].equalsIgnoreCase("item") && args[1].equalsIgnoreCase("give")) {
            try {
                WeaponType weapon = WeaponType.valueOf(args[2].toUpperCase(Locale.ROOT));
                Rarity rarity = Rarity.valueOf(args[3].toUpperCase(Locale.ROOT));
                dropService.give(player, itemFactory.createWeapon(weapon, rarity));
                Chat.send(player, rarity.korean() + " " + weapon.korean() + " 지급 완료.");
            } catch (IllegalArgumentException ex) {
                Chat.send(player, "사용법: /crpg item give <weaponType> <rarity>");
            }
            return true;
        }
        Chat.send(sender, "/crpg reload | mob spawn <mobId> | boss spawn forest_guardian | item give <weaponType> <rarity> | spawnpoint add <mobId> <maxAlive> <radius> | list/remove");
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("reload", "mob", "boss", "item", "spawnpoint");
        if (args.length == 2 && args[0].equalsIgnoreCase("mob")) return List.of("spawn");
        if (args.length == 3 && args[0].equalsIgnoreCase("mob")) return Arrays.stream(ForestMobType.values()).map(ForestMobType::id).toList();
        if (args.length == 2 && args[0].equalsIgnoreCase("boss")) return List.of("spawn");
        if (args.length == 3 && args[0].equalsIgnoreCase("boss")) return List.of(ForestGuardianBoss.ID);
        if (args.length == 2 && args[0].equalsIgnoreCase("item")) return List.of("give");
        if (args.length == 3 && args[0].equalsIgnoreCase("item")) return Arrays.stream(WeaponType.values()).map(Enum::name).map(String::toLowerCase).toList();
        if (args.length == 4 && args[0].equalsIgnoreCase("item")) return Arrays.stream(Rarity.values()).map(Enum::name).map(String::toLowerCase).toList();
        if (args.length == 2 && args[0].equalsIgnoreCase("spawnpoint")) return List.of("add", "list", "remove");
        if (args.length == 3 && args[0].equalsIgnoreCase("spawnpoint") && args[1].equalsIgnoreCase("add")) return Arrays.stream(ForestMobType.values()).map(ForestMobType::id).toList();
        if (args.length == 4 && args[0].equalsIgnoreCase("spawnpoint") && args[1].equalsIgnoreCase("add")) return List.of("5", "6", "10");
        if (args.length == 5 && args[0].equalsIgnoreCase("spawnpoint") && args[1].equalsIgnoreCase("add")) return List.of("8", "10", "12");
        if (args.length == 3 && args[0].equalsIgnoreCase("spawnpoint") && args[1].equalsIgnoreCase("remove")) return spawnPointManager.list().stream().map(point -> String.valueOf(point.id())).toList();
        return List.of();
    }
}
