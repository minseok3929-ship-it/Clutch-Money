package com.clutchrpg.mobs;

import com.clutchrpg.items.DropService;
import com.clutchrpg.player.PlayerManager;
import com.clutchrpg.util.Chat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public final class MobDeathListener implements Listener {
    private final MobManager mobManager;
    private final PlayerManager playerManager;
    private final DropService dropService;

    public MobDeathListener(MobManager mobManager, PlayerManager playerManager, DropService dropService) {
        this.mobManager = mobManager; this.playerManager = playerManager; this.dropService = dropService;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        ForestMobType type = mobManager.typeOf(entity);
        if (type == null) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        Player killer = entity.getKiller();
        if (killer != null) {
            playerManager.addExp(killer, type.exp());
            dropService.rollForestDrop(killer, type.level() / 100.0);
            Chat.send(killer, type.koreanName() + " 처치! 경험치 +" + type.exp());
        }
    }
}
