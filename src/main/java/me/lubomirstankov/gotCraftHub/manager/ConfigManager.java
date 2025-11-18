package me.lubomirstankov.gotCraftHub.manager;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {
    private final GotCraftHub plugin;
    private FileConfiguration config;
    private FileConfiguration hubItemsConfig;
    private final MiniMessage miniMessage;

    public ConfigManager(GotCraftHub plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadConfigs();
    }

    public void loadConfigs() {
        // Save default config.yml if it doesn't exist
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // Load hub-items.yml
        File hubItemsFile = new File(plugin.getDataFolder(), "hub-items.yml");
        if (!hubItemsFile.exists()) {
            saveResource("hub-items.yml");
        }
        hubItemsConfig = YamlConfiguration.loadConfiguration(hubItemsFile);
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        File hubItemsFile = new File(plugin.getDataFolder(), "hub-items.yml");
        hubItemsConfig = YamlConfiguration.loadConfiguration(hubItemsFile);
    }

    private void saveResource(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (InputStream in = plugin.getResource(resourcePath)) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Could not save " + resourcePath);
            }
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getHubItemsConfig() {
        return hubItemsConfig;
    }

    public String getHubWorld() {
        return config.getString("hub-world", "hub");
    }

    public boolean isDoubleJumpEnabled() {
        return config.getBoolean("double-jump.enabled", true);
    }

    public double getDoubleJumpPower() {
        return config.getDouble("double-jump.power", 1.2);
    }

    public double getDoubleJumpCooldown() {
        return config.getDouble("double-jump.cooldown", 1.0);
    }

    public boolean isPressurePlateBoostEnabled() {
        return config.getBoolean("pressure-plate-boost.enabled", true);
    }

    public double getPressurePlateHorizontal() {
        return config.getDouble("pressure-plate-boost.horizontal", 1.5);
    }

    public double getPressurePlateVertical() {
        return config.getDouble("pressure-plate-boost.vertical", 0.6);
    }

    public double getPressurePlateCooldown() {
        return config.getDouble("pressure-plate-boost.cooldown", 0.5);
    }

    public boolean isTrailsEnabled() {
        return config.getBoolean("trails.enabled", true);
    }

    public String getTrailParticle() {
        return config.getString("trails.particle", "END_ROD");
    }

    public int getTrailAmount() {
        return config.getInt("trails.amount", 2);
    }

    public int getTrailSpawnRate() {
        return config.getInt("trails.spawn-rate", 5);
    }

    public Component getMessage(String key) {
        String message = config.getString("messages." + key, "");
        return parseMessage(message);
    }

    public Component getMessageNoPrefix(String key) {
        String message = config.getString("messages." + key, "");
        return parseMessage(message);
    }

    public String colorize(String message) {
        return message.replace("&", "§");
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

