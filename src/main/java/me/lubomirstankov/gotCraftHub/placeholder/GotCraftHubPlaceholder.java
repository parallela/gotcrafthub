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
        return "gothub";
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
}

