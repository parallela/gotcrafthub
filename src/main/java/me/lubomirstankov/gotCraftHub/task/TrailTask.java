package me.lubomirstankov.gotCraftHub.task;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TrailTask extends BukkitRunnable {
    private final GotCraftHub plugin;

    public TrailTask(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfigManager().isTrailsEnabled()) {
            return;
        }

        String hubWorld = plugin.getConfigManager().getHubWorld();
        String particleName = plugin.getConfigManager().getTrailParticle();
        int amount = plugin.getConfigManager().getTrailAmount();

        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type: " + particleName);
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equalsIgnoreCase(hubWorld)) {
                continue;
            }

            if (plugin.getPlayerDataManager().isBuildModeEnabled(player)) {
                continue;
            }

            // Spawn particles at player's feet
            Location loc = player.getLocation().clone();
            loc.add(0, 0.1, 0);

            player.getWorld().spawnParticle(particle, loc, amount, 0.2, 0.1, 0.2, 0);
        }
    }
}

