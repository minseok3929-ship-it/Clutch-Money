package com.clutchrpg.items;

import com.clutchrpg.util.Keys;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class ItemFactory {
    private final Keys keys;
    private final Random random = new Random();

    public ItemFactory(Keys keys) { this.keys = keys; }

    public ItemStack createWeapon(WeaponType type, Rarity rarity) {
        ItemStack item = new ItemStack(type.material());
        ItemMeta meta = item.getItemMeta();
        String id = "forest_" + type.name().toLowerCase(Locale.ROOT) + "_" + System.nanoTime();
        double damage = type.baseDamage() + rarity.ordinal() * 3.5 + random.nextDouble(2.5);
        List<String> options = rollOptions(rarity);
        meta.displayName(Component.text(rarity.korean() + " 숲의 " + type.korean(), rarity.color()));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("울창한 숲 전투 프로토타입 장비", NamedTextColor.DARK_GRAY));
        lore.add(Component.text("공격력 +" + String.format(Locale.KOREA, "%.1f", damage), NamedTextColor.GRAY));
        lore.add(Component.text("요구 스텟: 없음", NamedTextColor.GRAY));
        lore.add(Component.text("옵션", NamedTextColor.AQUA));
        for (String option : options) lore.add(Component.text(" - " + option, NamedTextColor.LIGHT_PURPLE));
        if (rarity == Rarity.LEGENDARY) lore.add(Component.text("고유 효과 슬롯 준비됨", NamedTextColor.GOLD));
        if (rarity == Rarity.MYTHIC) lore.add(Component.text("플레이 스타일 변경 효과 슬롯 준비됨", NamedTextColor.RED));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(keys.itemId, PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(keys.itemTier, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(keys.rarity, PersistentDataType.STRING, rarity.name());
        meta.getPersistentDataContainer().set(keys.weaponType, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(keys.requiredStats, PersistentDataType.STRING, "{}");
        meta.getPersistentDataContainer().set(keys.randomOptions, PersistentDataType.STRING, String.join(";", options) + ";ATTACK_DAMAGE=" + damage);
        item.setItemMeta(meta);
        return item;
    }

    public WeaponType weaponType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String value = item.getItemMeta().getPersistentDataContainer().get(keys.weaponType, PersistentDataType.STRING);
        if (value == null) return null;
        try { return WeaponType.valueOf(value); } catch (IllegalArgumentException ex) { return null; }
    }

    public Rarity rarity(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Rarity.COMMON;
        String value = item.getItemMeta().getPersistentDataContainer().get(keys.rarity, PersistentDataType.STRING);
        if (value == null) return Rarity.COMMON;
        try { return Rarity.valueOf(value); } catch (IllegalArgumentException ex) { return Rarity.COMMON; }
    }

    public double weaponDamage(ItemStack item) {
        WeaponType type = weaponType(item);
        if (type == null) return 2.0;
        String options = item.getItemMeta().getPersistentDataContainer().get(keys.randomOptions, PersistentDataType.STRING);
        if (options != null) {
            for (String token : options.split(";")) {
                if (token.startsWith("ATTACK_DAMAGE=")) return Double.parseDouble(token.substring("ATTACK_DAMAGE=".length()));
            }
        }
        return type.baseDamage();
    }

    private List<String> rollOptions(Rarity rarity) {
        OptionType[] pool = OptionType.values();
        List<String> options = new ArrayList<>();
        int count = Math.min(rarity.optionSlots(), 3);
        for (int i = 0; i < count; i++) {
            OptionType type = pool[random.nextInt(pool.length)];
            double value = 1.0 + random.nextDouble() * (rarity.ordinal() + 1) * 2.5;
            options.add(type.name() + "=" + String.format(Locale.US, "%.2f", value) + " (" + type.korean() + ")");
        }
        return options;
    }
}
