package me.lubomirstankov.gotCraftHub.task;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HealthHungerTask extends BukkitRunnable {
    private final GotCraftHub plugin;

    public HealthHungerTask(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        String hubWorld = plugin.getConfigManager().getHubWorld();
        boolean healthLock = plugin.getConfigManager().getConfig().getBoolean("protection.health-lock", true);
        boolean hungerLock = !plugin.getConfigManager().getConfig().getBoolean("protection.hunger", false);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equalsIgnoreCase(hubWorld)) {
                continue;
            }

            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            if (healthLock && player.getHealth() < 20) {
                player.setHealth(20);
            }

            if (hungerLock) {
                if (player.getFoodLevel() < 20) {
                    player.setFoodLevel(20);
                }
                if (player.getSaturation() < 20) {
                    player.setSaturation(20);
                }
            }
        }
    }
}

