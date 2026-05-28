package com.clutchrpg.player;

import com.clutchrpg.items.ItemFactory;
import com.clutchrpg.items.Rarity;
import com.clutchrpg.items.WeaponType;
import com.clutchrpg.util.Chat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerLifecycleListener implements Listener {
    private final PlayerManager playerManager;
    private final ItemFactory itemFactory;

    public PlayerLifecycleListener(PlayerManager playerManager, ItemFactory itemFactory) {
        this.playerManager = playerManager; this.itemFactory = itemFactory;
    }

    @EventHandler public void onJoin(PlayerJoinEvent event) {
        playerManager.load(event.getPlayer());
        if (!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().getInventory().addItem(itemFactory.createWeapon(WeaponType.SWORD, Rarity.COMMON));
            event.getPlayer().getInventory().addItem(itemFactory.createWeapon(WeaponType.BOW, Rarity.COMMON));
            event.getPlayer().getInventory().addItem(itemFactory.createWeapon(WeaponType.STAFF, Rarity.COMMON));
        }
        Chat.send(event.getPlayer(), "울창한 숲 전투 MVP에 접속했습니다. /도움말 /스텟");
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) {
        playerManager.save(event.getPlayer());
    }
}
