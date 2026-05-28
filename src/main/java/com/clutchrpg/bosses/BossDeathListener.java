package com.clutchrpg.bosses;

import com.clutchrpg.items.DropService;
import com.clutchrpg.player.PlayerManager;
import com.clutchrpg.util.Chat;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public final class BossDeathListener implements Listener {
    private final ForestGuardianBoss boss;
    private final PlayerManager playerManager;
    private final DropService dropService;

    public BossDeathListener(ForestGuardianBoss boss, PlayerManager playerManager, DropService dropService) {
        this.boss = boss; this.playerManager = playerManager; this.dropService = dropService;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!boss.isBoss(entity)) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        Player killer = entity.getKiller();
        if (killer != null) {
            playerManager.addExp(killer, 450);
            for (int i = 0; i < 3; i++) dropService.rollForestDrop(killer, 0.18);
            killer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, killer.getLocation().add(0, 1, 0), 100, 0.8, 0.8, 0.8, 0.1);
            killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
            Chat.send(killer, "숲의 수호자를 쓰러뜨렸습니다! 보스 보상이 지급되었습니다.");
        }
    }
}
