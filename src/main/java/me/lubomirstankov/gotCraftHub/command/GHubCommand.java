package me.lubomirstankov.gotCraftHub.command;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GHubCommand implements CommandExecutor, TabCompleter {
    private final GotCraftHub plugin;
    private final MiniMessage miniMessage;

    public GHubCommand(GotCraftHub plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "build" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                if (!player.hasPermission("ghub.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }

                if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("not-in-hub"));
                    return true;
                }

                toggleBuildMode(player);
                return true;
            }
            case "hide" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("not-in-hub"));
                    return true;
                }

                togglePlayerVisibility(player);
                return true;
            }
            case "setspawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                if (!player.hasPermission("ghub.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }

                if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("not-in-hub"));
                    return true;
                }

                setSpawn(player);
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("ghub.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }

                plugin.getConfigManager().reload();
                plugin.getHubItemManager().loadHubItems();
                if (plugin.getSchematicDisplayManager() != null) {
                    plugin.getSchematicDisplayManager().reload();
                }
                sender.sendMessage(plugin.getConfigManager().getMessage("reload"));
                return true;
            }
            case "setdisplay" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                if (!player.hasPermission("ghub.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }

                setDisplayLocation(player);
                return true;
            }
            case "updatedisplay" -> {
                if (!sender.hasPermission("ghub.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }

                updateDisplay(sender);
                return true;
            }
            case "pos1" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                if (!player.hasPermission("ghub.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }

                setJumpPos1(player);
                return true;
            }
            case "pos2" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                if (!player.hasPermission("ghub.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }

                setJumpPos2(player);
                return true;
            }
            case "setjumprespawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                if (!player.hasPermission("ghub.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }

                setJumpRespawn(player);
                return true;
            }
            case "jump" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("not-in-hub"));
                    return true;
                }

                joinJump(player);
                return true;
            }
            case "leavejump", "leave" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
                    return true;
                }

                leaveJump(player);
                return true;
            }
            default -> {
                sendHelp(sender);
                return true;
            }
        }
    }

    private void toggleBuildMode(Player player) {
        boolean currentMode = plugin.getPlayerDataManager().isBuildModeEnabled(player);
        boolean newMode = !currentMode;

        plugin.getPlayerDataManager().setBuildMode(player, newMode);

        if (newMode) {
            player.sendMessage(plugin.getConfigManager().getMessage("build-mode-enabled"));
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("build-mode-disabled"));
            // Restore hub items
            plugin.getHubItemManager().giveHubItems(player);
        }
    }

    private void togglePlayerVisibility(Player player) {
        boolean currentlyHidden = plugin.getPlayerDataManager().arePlayersHidden(player);
        boolean newHidden = !currentlyHidden;

        plugin.getPlayerDataManager().setPlayersHidden(player, newHidden);

        if (newHidden) {
            // Hide all players
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    player.hidePlayer(plugin, online);
                }
            }
            player.sendMessage(plugin.getConfigManager().getMessage("players-hidden"));
        } else {
            // Show all players
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    player.showPlayer(plugin, online);
                }
            }
            player.sendMessage(plugin.getConfigManager().getMessage("players-shown"));
        }
    }

    private void setSpawn(Player player) {
        // Set the exact spawn location with player's pitch and yaw
        plugin.getSpawnManager().setSpawn(player.getLocation());
        player.sendMessage(plugin.getConfigManager().getMessage("spawn-set"));
    }

    private void setDisplayLocation(Player player) {
        if (plugin.getSchematicDisplayManager() == null) {
            player.sendMessage(miniMessage.deserialize(
                "<gradient:aqua:green>GOTCRAFT</gradient> <dark_gray>»</dark_gray> <red>WorldEdit is not installed! Schematic displays are disabled.</red>"
            ));
            return;
        }

        plugin.getSchematicDisplayManager().setDisplayLocation(player.getLocation());
        player.sendMessage(miniMessage.deserialize(
            "<gradient:aqua:green>GOTCRAFT</gradient> <dark_gray>»</dark_gray> <green>Schematic display location set to your current position!</green>"
        ));

        // Immediately update the display
        int currentPlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = plugin.getConfig().getInt("schematic-display.max-players", 100);
        plugin.getSchematicDisplayManager().updateDisplay(currentPlayers, maxPlayers);
    }

    private void updateDisplay(CommandSender sender) {
        if (plugin.getSchematicDisplayManager() == null) {
            sender.sendMessage(miniMessage.deserialize(
                "<gradient:aqua:green>GOTCRAFT</gradient> <dark_gray>»</dark_gray> <red>WorldEdit is not installed! Schematic displays are disabled.</red>"
            ));
            return;
        }

        int currentPlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = plugin.getConfig().getInt("schematic-display.max-players", 100);
        plugin.getSchematicDisplayManager().updateDisplay(currentPlayers, maxPlayers);

        sender.sendMessage(miniMessage.deserialize(
            "<gradient:aqua:green>GOTCRAFT</gradient> <dark_gray>»</dark_gray> <green>Schematic display updated! (Players: " + currentPlayers + "/" + maxPlayers + ")</green>"
        ));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gray>--------------------</gray> <gradient:aqua:green>GotCraftHub</gradient> <gray>--------------------</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ghub hide</yellow> <gray>- Toggle player visibility</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ghub jump</yellow> <gray>- Join the jump minigame</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ghub leave</yellow> <gray>- Leave the jump minigame</gray>"));
        if (sender.hasPermission("ghub.admin")) {
            sender.sendMessage(miniMessage.deserialize("<yellow>/ghub build</yellow> <gray>- Toggle build mode</gray>"));
            sender.sendMessage(miniMessage.deserialize("<yellow>/ghub setspawn</yellow> <gray>- Set hub spawn location</gray>"));
            sender.sendMessage(miniMessage.deserialize("<yellow>/ghub pos1</yellow> <gray>- Set jump area position 1</gray>"));
            sender.sendMessage(miniMessage.deserialize("<yellow>/ghub pos2</yellow> <gray>- Set jump area position 2</gray>"));
            sender.sendMessage(miniMessage.deserialize("<yellow>/ghub setjumprespawn</yellow> <gray>- Set jump respawn location</gray>"));
            sender.sendMessage(miniMessage.deserialize("<yellow>/ghub setdisplay</yellow> <gray>- Set schematic display location</gray>"));
            sender.sendMessage(miniMessage.deserialize("<yellow>/ghub updatedisplay</yellow> <gray>- Force update schematic display</gray>"));
            sender.sendMessage(miniMessage.deserialize("<yellow>/ghub reload</yellow> <gray>- Reload configuration</gray>"));
        }
        sender.sendMessage(miniMessage.deserialize("<gray>--------------------------------------------------</gray>"));
    }

    private void setJumpPos1(Player player) {
        plugin.getJumpManager().setPos1(player.getLocation());
        player.sendMessage(plugin.getConfigManager().getMessage("jump-pos1-set"));
    }

    private void setJumpPos2(Player player) {
        plugin.getJumpManager().setPos2(player.getLocation());
        player.sendMessage(plugin.getConfigManager().getMessage("jump-pos2-set"));
    }

    private void setJumpRespawn(Player player) {
        plugin.getJumpManager().setJumpRespawn(player.getLocation());
        player.sendMessage(plugin.getConfigManager().getMessage("jump-respawn-set"));
    }

    private void joinJump(Player player) {
        if (!plugin.getJumpManager().isSetupComplete()) {
            player.sendMessage(plugin.getConfigManager().getMessage("jump-not-setup"));
            return;
        }

        if (plugin.getJumpManager().isPlayerInGame(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("jump-already-in-game"));
            return;
        }

        if (!plugin.getJumpManager().canJoinGame()) {
            player.sendMessage(plugin.getConfigManager().getMessage("jump-game-full"));
            return;
        }

        if (plugin.getJumpManager().startJumpSession(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("jump-joined"));
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("jump-join-failed"));
        }
    }

    private void leaveJump(Player player) {
        if (!plugin.getJumpManager().isPlayerInGame(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("jump-not-in-game"));
            return;
        }

        plugin.getJumpManager().endJumpSession(player, true);
        player.sendMessage(plugin.getConfigManager().getMessage("jump-left"));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("hide");
            completions.add("jump");
            completions.add("leave");
            if (sender.hasPermission("ghub.admin")) {
                completions.add("build");
                completions.add("setspawn");
                completions.add("pos1");
                completions.add("pos2");
                completions.add("setjumprespawn");
                completions.add("setdisplay");
                completions.add("updatedisplay");
                completions.add("reload");
            }
        }

        return completions;
    }
}

