package me.lubomirstankov.gotCraftHub.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GotCraftHubPlaceholder extends PlaceholderExpansion {
    private final GotCraftHub plugin;

    public GotCraftHubPlaceholder(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "gotcraft";
    }

    @Override
    public @NotNull String getAuthor() {
        return "lubomirstankov";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // Handle jump leaderboard placeholders: %gotcraft_jump_1%, %gotcraft_jump_2%, etc.
        if (params.toLowerCase().startsWith("jump_")) {
            String[] parts = params.split("_");
            if (parts.length == 2) {
                try {
                    int position = Integer.parseInt(parts[1]);
                    var topPlayers = plugin.getJumpManager().getTopPlayers(position);
                    if (position > 0 && position <= topPlayers.size()) {
                        var entry = topPlayers.get(position - 1);
                        return entry.getKey() + " - " + entry.getValue();
                    }
                    return "N/A";
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        // Handle server-specific player counts: %gotcraft_servername%
        // This will match any server name directly as the parameter
        // Examples: %gotcraft_lobby%, %gotcraft_survival%, %gotcraft_creative%
        if (plugin.getBungeeCordManager() != null) {
            // Check if this might be a server name (not one of our known placeholders)
            if (!isKnownPlaceholder(params.toLowerCase())) {
                // Treat it as a server name
                int count = plugin.getBungeeCordManager().getServerPlayerCount(params);
                return String.valueOf(count);
            }
        }

        // Handle known placeholders
        switch (params.toLowerCase()) {
            case "is_hidden":
                return String.valueOf(plugin.getPlayerDataManager().arePlayersHidden(player));

            case "trail_enabled":
                return String.valueOf(plugin.getConfigManager().isTrailsEnabled());

            case "buildmode":
                return String.valueOf(plugin.getPlayerDataManager().isBuildModeEnabled(player));

            case "world":
                return player.getWorld().getName();

            case "players_visible":
                return String.valueOf(plugin.getPlayerDataManager().getVisiblePlayersCount(player));

            case "in_hub":
                return String.valueOf(player.getWorld().getName()
                        .equalsIgnoreCase(plugin.getConfigManager().getHubWorld()));

            case "online_players":
                return String.valueOf(plugin.getServer().getOnlinePlayers().size());

            case "total_players":
            case "bungee_total":
                // Total players across all BungeeCord servers
                if (plugin.getBungeeCordManager() != null) {
                    return String.valueOf(plugin.getBungeeCordManager().getTotalPlayers());
                }
                return String.valueOf(plugin.getServer().getOnlinePlayers().size());

            case "max_players":
                return String.valueOf(plugin.getConfig().getInt("schematic-display.max-players", 100));

            case "display_enabled":
                return plugin.getSchematicDisplayManager() != null
                        ? String.valueOf(plugin.getSchematicDisplayManager().isEnabled())
                        : "false";

            case "jump_score":
                return String.valueOf(plugin.getJumpManager().getPlayerScore(player.getUniqueId()));

            case "jump_rank":
                int rank = plugin.getJumpManager().getPlayerRank(player.getUniqueId());
                return rank > 0 ? String.valueOf(rank) : "N/A";

            case "jump_active_players":
                return String.valueOf(plugin.getJumpManager().getActivePlayerCount());

            case "jump_in_game":
                return String.valueOf(plugin.getJumpManager().isPlayerInGame(player));

            default:
                return null;
        }
    }

    /**
     * Check if the parameter is a known placeholder (not a server name)
     */
    private boolean isKnownPlaceholder(String param) {
        // Check if it starts with jump_ (for jump leaderboard)
        if (param.startsWith("jump_")) {
            return true;
        }

        switch (param) {
            case "is_hidden":
            case "trail_enabled":
            case "buildmode":
            case "world":
            case "players_visible":
            case "in_hub":
            case "online_players":
            case "total_players":
            case "bungee_total":
            case "max_players":
            case "display_enabled":
            case "jump_score":
            case "jump_rank":
            case "jump_active_players":
            case "jump_in_game":
                return true;
            default:
                return false;
        }
    }
}

