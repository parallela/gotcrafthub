package me.lubomirstankov.gotCraftHub.listener;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final GotCraftHub plugin;

    public PlayerListener(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if player is in hub world
        if (player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            // Teleport to spawn if enabled
            if (plugin.getConfigManager().getConfig().getBoolean("teleport-to-spawn-on-join", true)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Use custom spawn location if set, otherwise use world spawn
                    if (plugin.getSpawnManager().hasSpawn()) {
                        player.teleport(plugin.getSpawnManager().getSpawn());
                    } else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                }, 1L);
            }

            // Give hub items
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getConfigManager().getConfig().getBoolean("hub-items.enabled", true)) {
                    plugin.getHubItemManager().giveHubItems(player);
                }

                // Set health and hunger
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);
            }, 5L);

            // Update visibility for existing players
            updatePlayerVisibility(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().clearPlayerData(event.getPlayer());
    }

    private void updatePlayerVisibility(Player joiningPlayer) {
        // Hide/show the joining player for existing players based on their settings
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(joiningPlayer)) continue;

            if (plugin.getPlayerDataManager().arePlayersHidden(online)) {
                online.hidePlayer(plugin, joiningPlayer);
            }

            if (plugin.getPlayerDataManager().arePlayersHidden(joiningPlayer)) {
                joiningPlayer.hidePlayer(plugin, online);
            }
        }
    }
}

