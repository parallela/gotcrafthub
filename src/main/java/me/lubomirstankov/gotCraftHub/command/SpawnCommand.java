package me.lubomirstankov.gotCraftHub.command;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private final GotCraftHub plugin;

    public SpawnCommand(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Ensure command only works in hub world if configured that way
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            player.sendMessage("You are not in the hub world.");
            return true;
        }

        Location target;
        if (plugin.getSpawnManager().hasSpawn()) {
            target = plugin.getSpawnManager().getSpawn();
        } else {
            World world = Bukkit.getWorld(plugin.getConfigManager().getHubWorld());
            target = world != null ? world.getSpawnLocation() : player.getWorld().getSpawnLocation();
        }

        player.teleport(target);
        player.sendMessage("Teleported to spawn.");
        return true;
    }
}

