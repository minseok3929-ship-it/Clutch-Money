package com.clutchrpg.commands;

import com.clutchrpg.gui.StatsGui;
import com.clutchrpg.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PlayerCommands implements CommandExecutor {
    private final StatsGui statsGui;
    public PlayerCommands(StatsGui statsGui) { this.statsGui = statsGui; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Chat.send(sender, "플레이어만 사용할 수 있습니다.");
            return true;
        }
        if (label.equals("스텟")) {
            statsGui.open(player);
            return true;
        }
        if (label.equals("가방")) {
            Chat.send(player, "몬스터 드랍은 인벤토리로 즉시 지급됩니다. 공간이 없으면 발밑에 드랍됩니다.");
            return true;
        }
        Chat.send(player, "좌클릭으로 무기 기본 공격, 점프 중 Shift로 대쉬, /스텟으로 성장하세요.");
        return true;
    }
}
