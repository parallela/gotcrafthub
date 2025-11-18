package me.lubomirstankov.gotCraftHub.listener;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandFilterListener implements Listener {
    private final GotCraftHub plugin;
    private final MiniMessage miniMessage;
    
    // List of blocked commands (without the leading slash)
    private final List<String> blockedCommands = Arrays.asList(
        // General dangerous commands
        "op",
        "deop",
        "plugins",
        "pl",

        // Information leak commands
        "version",
        "ver",
        "about",
        "icanhasbukkit",
        "?",
        "help",

        // Namespaced commands - specific
        "bukkit:plugins",
        "bukkit:pl",
        "bukkit:help",
        "bukkit:?",
        "bukkit:about",
        "bukkit:version",
        "bukkit:ver",

        "minecraft:help",
        "minecraft:plugins",
        "minecraft:pl",

        "paper:paper",
        "paper:version",
        "paper:ver",

        // Namespace wildcards (block ALL commands under these namespaces)
        "bukkit:",
        "minecraft:",
        "paper:"
    );

    public CommandFilterListener(GotCraftHub plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Check if command filtering is enabled
        if (!plugin.getConfigManager().getConfig().getBoolean("command-filtering.enabled", true)) {
            return;
        }

        // Only filter in hub world
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        // Skip if player has admin permission
        if (player.hasPermission("ghub.admin")) {
            return;
        }

        String message = event.getMessage().toLowerCase();
        // Remove the leading slash
        String command = message.substring(1);

        // Extract the base command (before any arguments)
        String baseCommand = command.split(" ")[0];

        // Check if the command is blocked
        if (isBlocked(baseCommand)) {
            event.setCancelled(true);

            // Send custom "unknown command" message mimicking vanilla Minecraft
            player.sendMessage(miniMessage.deserialize(
                    "<red>Unknown or incomplete command, see below for error\n"
                            + baseCommand +"[<-HERE]</red>"
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();

        // Check if command filtering is enabled
        if (!plugin.getConfigManager().getConfig().getBoolean("command-filtering.enabled", true)) {
            return;
        }

        // Only filter in hub world
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        // Skip if player has admin permission
        if (player.hasPermission("ghub.admin")) {
            return;
        }

        // Remove blocked commands from tab completion
        Set<String> commandsToRemove = new HashSet<>();
        for (String cmd : event.getCommands()) {
            if (isBlocked(cmd.toLowerCase())) {
                commandsToRemove.add(cmd);
            }
        }
        event.getCommands().removeAll(commandsToRemove);
    }

    private boolean isBlocked(String command) {
        // Direct match
        if (blockedCommands.contains(command)) {
            return true;
        }

        // Check for namespace patterns (e.g., if command is "bukkit:pl" or starts with "bukkit:")
        for (String blocked : blockedCommands) {
            // Exact match
            if (command.equals(blocked)) {
                return true;
            }

            // Check if command starts with the blocked pattern
            if (command.startsWith(blocked + " ")) {
                return true;
            }

            // Special handling for namespace blocking (e.g., block all "bukkit:" commands)
            if (blocked.endsWith(":") && command.startsWith(blocked)) {
                return true;
            }
        }

        return false;
    }
}

