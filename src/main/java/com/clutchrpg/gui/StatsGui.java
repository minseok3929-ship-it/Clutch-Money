package com.clutchrpg.gui;

import com.clutchrpg.player.PlayerManager;
import com.clutchrpg.player.PlayerProfile;
import com.clutchrpg.stats.StatCalculator;
import com.clutchrpg.stats.StatType;
import com.clutchrpg.util.Chat;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class StatsGui implements Listener {
    public static final Component TITLE = Component.text("CLUTCH RPG 스텟", Chat.PURPLE);
    private final PlayerManager playerManager;

    public StatsGui(PlayerManager playerManager) { this.playerManager = playerManager; }

    public void open(Player player) {
        PlayerProfile profile = playerManager.get(player);
        Inventory inv = Bukkit.createInventory(player, 27, TITLE);
        inv.setItem(4, item(Material.NETHER_STAR, "Lv." + profile.level() + " 성장 정보", "경험치: " + profile.exp() + " / " + StatCalculator.expNeeded(profile.level()), "남은 스텟 포인트: " + profile.statPoints()));
        int[] slots = {10, 11, 12, 13, 14};
        StatType[] types = StatType.values();
        for (int i = 0; i < types.length; i++) {
            StatType type = types[i];
            inv.setItem(slots[i], item(Material.LIME_DYE, type.name() + " (" + type.korean() + ")", "현재: " + profile.stat(type), statPreview(type), "좌클릭 +1 / Shift 좌클릭 +5"));
        }
        inv.setItem(22, item(Material.HEART_OF_THE_SEA, "전투 파생 수치",
                "치명타 확률: " + pct(StatCalculator.critChance(profile)),
                "이동속도 보너스: " + pct(StatCalculator.moveSpeedBonus(profile)),
                "현재 체력: " + String.format(Locale.KOREA, "%.0f / %.0f", player.getHealth(), StatCalculator.maxHealth(profile))));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(TITLE)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        StatType type = switch (event.getRawSlot()) {
            case 10 -> StatType.STR;
            case 11 -> StatType.DEX;
            case 12 -> StatType.INT;
            case 13 -> StatType.VIT;
            case 14 -> StatType.LUK;
            default -> null;
        };
        if (type == null) return;
        int amount = event.getClick() == ClickType.SHIFT_LEFT ? 5 : 1;
        if (!playerManager.get(player).addStat(type, amount)) {
            Chat.send(player, "스텟 포인트가 부족합니다.");
            return;
        }
        playerManager.applyDerivedStats(player);
        Chat.send(player, type.korean() + " +" + amount + " 증가! " + statPreview(type));
        open(player);
    }

    private String statPreview(StatType type) {
        return switch (type) {
            case STR -> "투자 효과: 공격력 +0.8 / 체력 +2";
            case DEX -> "투자 효과: 치명타 확률 +0.15% / 이동속도 +0.05%";
            case INT -> "투자 효과: 스킬 피해 +1.0% / 상태이상 피해 +0.5%";
            case VIT -> "투자 효과: 체력 +6 / 상태이상 저항 +0.1%";
            case LUK -> "투자 효과: 드랍률 +0.1% / 희귀 장비 +0.03%";
        };
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, Chat.CYAN));
        meta.lore(java.util.Arrays.stream(lore).map(s -> Component.text(s, NamedTextColor.GRAY)).toList());
        item.setItemMeta(meta);
        return item;
    }

    private String pct(double value) { return String.format(Locale.KOREA, "%.2f%%", value * 100.0); }
}
