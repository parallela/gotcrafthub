package me.lubomirstankov.gotCraftHub.manager;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SpawnManager {
    private final GotCraftHub plugin;
    private Location spawnLocation;
    private File spawnFile;

    public SpawnManager(GotCraftHub plugin) {
        this.plugin = plugin;
        this.spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        loadSpawn();
    }

    public void setSpawn(Location location) {
        this.spawnLocation = location.clone();
        saveSpawn();

        // Also set the world spawn for vanilla behavior
        location.getWorld().setSpawnLocation(location);
    }

    public Location getSpawn() {
        return spawnLocation != null ? spawnLocation.clone() : null;
    }

    public boolean hasSpawn() {
        return spawnLocation != null;
    }

    private void loadSpawn() {
        if (!spawnFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(spawnFile);

        if (!config.contains("spawn.world")) {
            return;
        }

        String worldName = config.getString("spawn.world");
        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");

        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            spawnLocation = new Location(world, x, y, z, yaw, pitch);
        }
    }

    private void saveSpawn() {
        if (spawnLocation == null) {
            return;
        }

        FileConfiguration config = new YamlConfiguration();
        config.set("spawn.world", spawnLocation.getWorld().getName());
        config.set("spawn.x", spawnLocation.getX());
        config.set("spawn.y", spawnLocation.getY());
        config.set("spawn.z", spawnLocation.getZ());
        config.set("spawn.yaw", spawnLocation.getYaw());
        config.set("spawn.pitch", spawnLocation.getPitch());

        try {
            if (!spawnFile.exists()) {
                spawnFile.getParentFile().mkdirs();
                spawnFile.createNewFile();
            }
            config.save(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawn location: " + e.getMessage());
        }
    }
}

