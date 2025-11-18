package me.lubomirstankov.gotCraftHub.task;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DoubleJumpTask extends BukkitRunnable {
    private final GotCraftHub plugin;

    public DoubleJumpTask(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfigManager().isDoubleJumpEnabled()) {
            return;
        }

        String hubWorld = plugin.getConfigManager().getHubWorld();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equalsIgnoreCase(hubWorld)) {
                continue;
            }

            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            // Enable flying (for double jump detection) when player is on ground
            if (player.isOnGround() && !player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.setFlying(false);
            }
        }
    }
}
