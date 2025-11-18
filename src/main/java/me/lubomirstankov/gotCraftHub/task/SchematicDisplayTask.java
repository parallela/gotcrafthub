package me.lubomirstankov.gotCraftHub.task;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SchematicDisplayTask extends BukkitRunnable {
    private final GotCraftHub plugin;

    public SchematicDisplayTask(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getSchematicDisplayManager().isEnabled()) {
            return;
        }

        // Get current player count - use BungeeCord total if enabled, otherwise local server
        int currentPlayers;
        if (plugin.getBungeeCordManager() != null) {
            currentPlayers = plugin.getBungeeCordManager().getTotalPlayers();
        } else {
            currentPlayers = Bukkit.getOnlinePlayers().size();
        }

        int maxPlayers = plugin.getConfig().getInt("schematic-display.max-players", 100);

        plugin.getSchematicDisplayManager().updateDisplay(currentPlayers, maxPlayers);
    }
}

