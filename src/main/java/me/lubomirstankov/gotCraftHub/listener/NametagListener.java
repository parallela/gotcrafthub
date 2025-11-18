package me.lubomirstankov.gotCraftHub.listener;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagListener implements Listener {
    private final GotCraftHub plugin;
    private final MiniMessage miniMessage;

    public NametagListener(GotCraftHub plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if player is in hub world
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        // Delay to ensure player is fully loaded
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Apply gradient nametag to the joining player
            setPlayerNametag(player);

            // Refresh nametags for all existing players
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
                    if (!online.equals(player)) {
                        setPlayerNametag(online);
                    }
                }
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove player from team
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("ghub_gradient");
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }

    private void setPlayerNametag(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("ghub_gradient");

        if (team == null) {
            team = scoreboard.registerNewTeam("ghub_gradient");
        }

        // Create gradient name component
        Component gradientName = miniMessage.deserialize("<gradient:aqua:green>" + player.getName() + "</gradient>");

        // Set the prefix with gradient
        team.prefix(gradientName);
        team.suffix(Component.empty());

        // Add player to team
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        // Also set display name and tab list name
        player.displayName(gradientName);
        player.playerListName(gradientName);
    }
}

