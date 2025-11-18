package me.lubomirstankov.gotCraftHub.manager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BungeeCordManager implements PluginMessageListener {
    private final GotCraftHub plugin;
    private int totalPlayers = 0;
    private final Map<String, Integer> serverPlayerCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> serverLastUpdate = new HashMap<>();
    private long lastGlobalUpdate = 0;
    private static final long UPDATE_INTERVAL = 3000; // 3 seconds

    public BungeeCordManager(GotCraftHub plugin) {
        this.plugin = plugin;

        // Register plugin messaging channel
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);

        plugin.getLogger().info("BungeeCord integration enabled");
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();

            if (subchannel.equals("PlayerCount")) {
                // Check if there's more data available
                try {
                    String server = in.readUTF(); // ALL or specific server name
                    int playerCount = in.readInt();

                    if (server.equals("ALL")) {
                        totalPlayers = playerCount;
                        plugin.getLogger().info("BungeeCord total player count updated: " + totalPlayers);
                    } else {
                        // Store individual server count
                        serverPlayerCounts.put(server, playerCount);
                        plugin.getLogger().info("BungeeCord server '" + server + "' player count: " + playerCount);
                    }
                } catch (Exception e) {
                    // Not enough data in message, ignore silently
                    // This happens when BungeeCord sends other messages
                }
            }
            // Ignore other subchannels we didn't request
        } catch (Exception e) {
            // Silently ignore malformed messages or messages we don't understand
            // This is normal - BungeeCord sends various plugin messages that we don't need to handle
        }
    }

    /**
     * Request the total player count from BungeeCord
     */
    public void requestPlayerCount() {
        // Don't spam requests
        long now = System.currentTimeMillis();
        if (now - lastGlobalUpdate < UPDATE_INTERVAL) {
            return;
        }
        lastGlobalUpdate = now;

        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        if (players.isEmpty()) {
            return;
        }

        // Get any online player to send the message through
        Player player = players.iterator().next();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF("ALL");

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /**
     * Request player count for a specific server
     * @param serverName The name of the server to query
     */
    public void requestServerPlayerCount(String serverName) {
        // Don't spam requests for this server
        long now = System.currentTimeMillis();
        Long lastUpdate = serverLastUpdate.get(serverName);
        if (lastUpdate != null && now - lastUpdate < UPDATE_INTERVAL) {
            return;
        }
        serverLastUpdate.put(serverName, now);

        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        if (players.isEmpty()) {
            return;
        }

        // Get any online player to send the message through
        Player player = players.iterator().next();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(serverName);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /**
     * Get the total player count across all BungeeCord servers
     * @return Total player count, or local server count if BungeeCord is disabled
     */
    public int getTotalPlayers() {
        if (plugin.getConfig().getBoolean("bungeecord.enabled", false)) {
            requestPlayerCount();
            return totalPlayers > 0 ? totalPlayers : plugin.getServer().getOnlinePlayers().size();
        }
        return plugin.getServer().getOnlinePlayers().size();
    }

    /**
     * Get the player count for a specific server
     * @param serverName The name of the server
     * @return Player count for the server, or 0 if unknown
     */
    public int getServerPlayerCount(String serverName) {
        if (!plugin.getConfig().getBoolean("bungeecord.enabled", false)) {
            return 0;
        }

        // Request update for this server
        requestServerPlayerCount(serverName);

        // Return cached count or 0
        return serverPlayerCounts.getOrDefault(serverName, 0);
    }

    public void disable() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, "BungeeCord");
    }
}

