package com.clutch.rpg.items;

import com.clutch.rpg.stats.StatType;
import com.clutch.rpg.util.Keys;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomItemFactory {
    private final Keys keys;
    private final Random random = new Random();

    public CustomItemFactory(Keys keys) {
        this.keys = keys;
    }

    public ItemStack createWeapon(WeaponType weaponType, Rarity rarity, int tier) {
        ItemStack item = new ItemStack(weaponType.material());
        ItemMeta meta = item.getItemMeta();
        String affix = rarity.hasAffixes() ? "폭풍의 " : "";
        meta.setDisplayName(rarity.color() + affix + readable(weaponType.name()) + ChatColor.DARK_GRAY + " [T" + tier + "]");
        RequiredStats requiredStats = new RequiredStats();
        requiredStats.set(primaryStat(weaponType), Math.max(0, tier * 3));
        List<ItemOption> options = rollOptions(rarity, weaponType);

        meta.getPersistentDataContainer().set(keys.itemId, PersistentDataType.STRING, "forest_" + weaponType.name().toLowerCase(Locale.ROOT) + "_" + UUID.randomUUID());
        meta.getPersistentDataContainer().set(keys.itemTier, PersistentDataType.INTEGER, tier);
        meta.getPersistentDataContainer().set(keys.rarity, PersistentDataType.STRING, rarity.name());
        meta.getPersistentDataContainer().set(keys.weaponType, PersistentDataType.STRING, weaponType.name());
        meta.getPersistentDataContainer().set(keys.requiredStats, PersistentDataType.STRING, requiredStats.serialize());
        meta.getPersistentDataContainer().set(keys.randomOptions, PersistentDataType.STRING, options.stream().map(ItemOption::serialize).collect(Collectors.joining(",")));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "CLUTCH RPG Prototype Weapon");
        lore.add(ChatColor.GRAY + "공격력: " + ChatColor.AQUA + String.format(Locale.US, "%.1f", baseAttack(item) + tier));
        lore.add(ChatColor.GRAY + "요구 스텟: " + ChatColor.LIGHT_PURPLE + requiredStats.asMap());
        lore.add(ChatColor.DARK_GRAY + "옵션");
        for (ItemOption option : options) {
            lore.add(ChatColor.GRAY + "- " + ChatColor.AQUA + option.type().name() + " +" + String.format(Locale.US, "%.1f", option.value()));
        }
        if (rarity.hasUniqueEffect()) {
            lore.add(ChatColor.GOLD + "고유 효과 슬롯 준비됨");
        }
        if (rarity.canChangePlayStyle()) {
            lore.add(ChatColor.DARK_PURPLE + "플레이 스타일 변경 효과 슬롯 준비됨");
        }
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private List<ItemOption> rollOptions(Rarity rarity, WeaponType weaponType) {
        List<OptionType> pool = new ArrayList<>(List.of(OptionType.values()));
        Collections.shuffle(pool, random);
        int count = rarity == Rarity.MYTHIC ? 4 + random.nextInt(2) : rarity.optionCount();
        List<ItemOption> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            OptionType type = pool.get(i);
            double value = switch (type) {
                case ATTACK_DAMAGE -> 1.0 + random.nextInt(4);
                case SKILL_DAMAGE, CRIT_DAMAGE, BLEED_DAMAGE, SHOCK_DAMAGE, BURN_DAMAGE -> 4.0 + random.nextInt(8);
                case CRIT_CHANCE, ARMOR_PENETRATION, DAMAGE_REDUCTION, DROP_RATE, GOLD_GAIN, DASH_COOLDOWN_REDUCTION -> 1.0 + random.nextInt(5);
                case MAX_HEALTH -> 4.0 + random.nextInt(10);
                case MOVE_SPEED -> 0.01 + random.nextDouble() * 0.03;
                case FROST_DURATION -> 0.4 + random.nextDouble();
            };
            result.add(new ItemOption(type, value));
        }
        if (weaponType == WeaponType.STAFF && result.stream().noneMatch(option -> option.type() == OptionType.SKILL_DAMAGE)) {
            result.set(0, new ItemOption(OptionType.SKILL_DAMAGE, 8.0));
        }
        return result;
    }

    public WeaponType getWeaponType(ItemStack item) {
        String raw = stringTag(item, keys.weaponType);
        if (raw == null) {
            return null;
        }
        return WeaponType.valueOf(raw);
    }

    public Rarity getRarity(ItemStack item) {
        String raw = stringTag(item, keys.rarity);
        return raw == null ? null : Rarity.valueOf(raw);
    }

    public List<ItemOption> getOptions(ItemStack item) {
        String raw = stringTag(item, keys.randomOptions);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<ItemOption> options = new ArrayList<>();
        for (String token : raw.split(",")) {
            options.add(ItemOption.parse(token));
        }
        return options;
    }

    public double baseAttack(ItemStack item) {
        WeaponType type = getWeaponType(item);
        Integer tier = item != null && item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().get(keys.itemTier, PersistentDataType.INTEGER) : null;
        return (type == null ? 1.0 : type.baseAttack()) + (tier == null ? 0 : tier);
    }

    public double optionSum(ItemStack item, OptionType type) {
        return getOptions(item).stream().filter(option -> option.type() == type).mapToDouble(ItemOption::value).sum();
    }

    public RequiredStats requiredStats(ItemStack item) {
        return RequiredStats.parse(stringTag(item, keys.requiredStats));
    }

    private String stringTag(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private StatType primaryStat(WeaponType type) {
        return switch (type) {
            case SWORD, HAMMER, SHIELD -> StatType.STR;
            case BOW -> StatType.DEX;
            case STAFF -> StatType.INT;
        };
    }

    private String readable(String name) {
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }
}
