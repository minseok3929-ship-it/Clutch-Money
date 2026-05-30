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
        List<String> optionData = rollOptionData(rarity);
        int requiredStr = 4 + rarity.ordinal() * 3 + (type == WeaponType.SWORD ? 3 : 0);
        int requiredDex = 3 + rarity.ordinal() * 2 + (type == WeaponType.BOW ? 4 : 0);
        int requiredInt = type == WeaponType.STAFF ? 6 + rarity.ordinal() * 3 : 0;
        meta.displayName(Component.text("[" + rarity.korean() + "] 숲의 " + type.korean(), rarity.color()));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("무기 종류: " + type.korean(), NamedTextColor.GRAY));
        lore.add(Component.text("장비 티어: T1", NamedTextColor.GRAY));
        lore.add(Component.text("요구 힘: " + requiredStr, NamedTextColor.DARK_GRAY));
        lore.add(Component.text("요구 민첩: " + requiredDex, NamedTextColor.DARK_GRAY));
        if (requiredInt > 0) lore.add(Component.text("요구 지력: " + requiredInt, NamedTextColor.DARK_GRAY));
        lore.add(Component.empty());
        lore.add(Component.text("공격력 +" + format(damage), NamedTextColor.WHITE));
        List<String> normalOptions = optionData.stream().filter(option -> !isSpecialOption(option)).toList();
        List<String> specialOptions = optionData.stream().filter(this::isSpecialOption).toList();
        for (String option : normalOptions) lore.add(Component.text(displayOption(option), NamedTextColor.WHITE));
        if (!specialOptions.isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("특수 옵션:", rarity.color()));
            for (String option : specialOptions) lore.add(Component.text("- " + displayOption(option), NamedTextColor.LIGHT_PURPLE));
        }
        lore.add(Component.empty());
        lore.add(Component.text("아이템 설명:", NamedTextColor.GRAY));
        lore.add(Component.text("울창한 숲의 마력이 깃든 " + type.korean() + "입니다.", NamedTextColor.DARK_GREEN));
        if (rarity == Rarity.LEGENDARY) lore.add(Component.text("전설 고유 효과 슬롯 준비됨", NamedTextColor.GOLD));
        if (rarity == Rarity.MYTHIC) lore.add(Component.text("신화 플레이 스타일 변경 효과 슬롯 준비됨", NamedTextColor.RED));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(keys.itemId, PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(keys.itemTier, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(keys.rarity, PersistentDataType.STRING, rarity.name());
        meta.getPersistentDataContainer().set(keys.weaponType, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(keys.requiredStats, PersistentDataType.STRING, "STR=" + requiredStr + ";DEX=" + requiredDex + ";INT=" + requiredInt + ";VIT=0;LUK=0");
        meta.getPersistentDataContainer().set(keys.randomOptions, PersistentDataType.STRING, String.join(";", optionData) + ";ATTACK_DAMAGE=" + damage);
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

    private List<String> rollOptionData(Rarity rarity) {
        OptionType[] pool = OptionType.values();
        List<String> options = new ArrayList<>();
        int count = Math.min(rarity.optionSlots(), 3);
        for (int i = 0; i < count; i++) {
            OptionType type = pool[random.nextInt(pool.length)];
            double value = type == OptionType.MAX_HEALTH ? 8.0 + random.nextDouble() * (rarity.ordinal() + 1) * 8.0 : 1.0 + random.nextDouble() * (rarity.ordinal() + 1) * 2.5;
            options.add(type.name() + "=" + String.format(Locale.US, "%.2f", value));
        }
        return options;
    }


    private boolean isSpecialOption(String data) {
        String key = data.split("=")[0];
        return key.equals(OptionType.BLEED_DAMAGE.name())
                || key.equals(OptionType.SHOCK_DAMAGE.name())
                || key.equals(OptionType.BURN_DAMAGE.name())
                || key.equals(OptionType.FROST_DURATION.name())
                || key.equals(OptionType.ARMOR_PENETRATION.name())
                || key.equals(OptionType.DASH_COOLDOWN_REDUCTION.name());
    }

    private String displayOption(String data) {
        String[] parts = data.split("=");
        if (parts.length != 2) return data;
        try {
            OptionType type = OptionType.valueOf(parts[0]);
            double value = Double.parseDouble(parts[1]);
            return type.korean() + " +" + format(value) + (type.percent() ? "%" : "");
        } catch (IllegalArgumentException ex) {
            return data;
        }
    }

    private String format(double value) {
        return String.format(Locale.KOREA, "%.1f", value);
    }
}
