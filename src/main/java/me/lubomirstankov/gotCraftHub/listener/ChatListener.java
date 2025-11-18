package me.lubomirstankov.gotCraftHub.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lubomirstankov.gotCraftHub.GotCraftHub;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {
    private final GotCraftHub plugin;
    private final boolean placeholderApiAvailable;
    private final MiniMessage miniMessage;

    public ChatListener(GotCraftHub plugin) {
        this.plugin = plugin;
        this.placeholderApiAvailable = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Only modify join message if in hub world
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        if (!plugin.getConfigManager().getConfig().getBoolean("join-leave-messages.enabled", true)) {
            return;
        }

        String joinMessage = plugin.getConfigManager().getConfig().getString("join-leave-messages.join",
                "<gray>[<green>+</green>]</gray> <white>%player_name%</white> <green>joined the hub</green>");

        joinMessage = joinMessage.replace("%player_name%", player.getName());

        if (placeholderApiAvailable) {
            joinMessage = PlaceholderAPI.setPlaceholders(player, joinMessage);
        }

        event.joinMessage(parseMessage(joinMessage));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Only modify quit message if in hub world
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        if (!plugin.getConfigManager().getConfig().getBoolean("join-leave-messages.enabled", true)) {
            return;
        }

        String leaveMessage = plugin.getConfigManager().getConfig().getString("join-leave-messages.leave",
                "<gray>[<red>-</red>]</gray> <white>%player_name%</white> <red>left the hub</red>");

        leaveMessage = leaveMessage.replace("%player_name%", player.getName());

        if (placeholderApiAvailable) {
            leaveMessage = PlaceholderAPI.setPlaceholders(player, leaveMessage);
        }

        event.quitMessage(parseMessage(leaveMessage));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // Only modify chat format if in hub world
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        if (!plugin.getConfigManager().getConfig().getBoolean("chat-format.enabled", true)) {
            return;
        }

        event.setCancelled(true);

        String format = plugin.getConfigManager().getConfig().getString("chat-format.format",
                "<gray>%player_name%</gray> <dark_gray>»</dark_gray> <white>%message%</white>");

        // Get the plain text message
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Replace placeholders
        format = format.replace("%player_name%", player.getName())
                       .replace("%message%", message);

        if (placeholderApiAvailable) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        // Broadcast the formatted message
        Component formattedMessage = parseMessage(format);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(formattedMessage);
        }
    }

    /**
     * Parse message with support for both MiniMessage and legacy color codes
     * Tries MiniMessage first, falls back to legacy & codes if that fails
     */
    private Component parseMessage(String text) {
        try {
            // Try MiniMessage format first
            return miniMessage.deserialize(text);
        } catch (Exception e) {
            // Fallback to legacy color codes (&)
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
    }
}

