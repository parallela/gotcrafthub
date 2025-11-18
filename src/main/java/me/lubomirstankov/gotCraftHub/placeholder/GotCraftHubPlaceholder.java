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

            default:
                return null;
        }
    }

    /**
     * Check if the parameter is a known placeholder (not a server name)
     */
    private boolean isKnownPlaceholder(String param) {
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
                return true;
            default:
                return false;
        }
    }
}

