package me.lubomirstankov.gotCraftHub.command;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BlockedCommand implements CommandExecutor {
    private final GotCraftHub plugin;
    private final MiniMessage miniMessage;

    public BlockedCommand(GotCraftHub plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Allow if sender has admin permission
        if (sender.hasPermission("ghub.admin")) {
            return false; // Let Bukkit handle it
        }

        // Only block in hub world for players
        if (sender instanceof Player player) {
            if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
                return false; // Let Bukkit handle it
            }

            // Check if command filtering is enabled
            if (!plugin.getConfigManager().getConfig().getBoolean("command-filtering.enabled", true)) {
                return false; // Let Bukkit handle it
            }

            // Block the command and send custom message

            player.sendMessage(miniMessage.deserialize(
                "<red>Unknown or incomplete command, see below for error\n"
                        + label +"[<-HERE]</red>"
            ));
            return true; // Command handled
        }

        return false; // Let console use commands
    }
}

