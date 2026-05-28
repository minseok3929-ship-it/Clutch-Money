package com.clutch.rpg.combat;

import com.clutch.rpg.items.CustomItemFactory;
import com.clutch.rpg.items.WeaponType;
import com.clutch.rpg.player.PlayerDataManager;
import com.clutch.rpg.skills.WeaponSkillService;
import com.clutch.rpg.util.MessageUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class CombatListener implements Listener {
    private final CustomItemFactory itemFactory;
    private final PlayerDataManager playerDataManager;
    private final DamageCalculator damageCalculator;
    private final WeaponSkillService weaponSkillService;
    private final DashService dashService;

    public CombatListener(CustomItemFactory itemFactory, PlayerDataManager playerDataManager, DamageCalculator damageCalculator, WeaponSkillService weaponSkillService, DashService dashService) {
        this.itemFactory = itemFactory;
        this.playerDataManager = playerDataManager;
        this.damageCalculator = damageCalculator;
        this.weaponSkillService = weaponSkillService;
        this.dashService = dashService;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (itemFactory.getWeaponType(item) != null) {
                event.setCancelled(true);
                if (!itemFactory.requiredStats(item).meets(playerDataManager.get(event.getPlayer()).stats())) {
                    MessageUtil.send(event.getPlayer(), "&7요구 스텟이 부족합니다.");
                    return;
                }
                weaponSkillService.cast(event.getPlayer(), item);
            }
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        WeaponType type = itemFactory.getWeaponType(item);
        if (type == null) {
            return;
        }
        if (!itemFactory.requiredStats(item).meets(playerDataManager.get(player).stats())) {
            event.setCancelled(true);
            MessageUtil.send(player, "&7요구 스텟이 부족해 무기를 사용할 수 없습니다.");
            return;
        }
        DamageCalculator.DamageRoll roll = damageCalculator.roll(playerDataManager.get(player), item, type == WeaponType.BOW ? 0.85 : 1.0);
        event.setDamage(roll.damage());
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            dashService.tryDash(event.getPlayer());
        }
    }
}
