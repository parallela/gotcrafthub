package me.lubomirstankov.gotCraftHub;

import me.lubomirstankov.gotCraftHub.command.BlockedCommand;
import me.lubomirstankov.gotCraftHub.command.GHubCommand;
import me.lubomirstankov.gotCraftHub.command.SpawnCommand;
import me.lubomirstankov.gotCraftHub.listener.*;
import me.lubomirstankov.gotCraftHub.manager.BungeeCordManager;
import me.lubomirstankov.gotCraftHub.manager.ConfigManager;
import me.lubomirstankov.gotCraftHub.manager.HubItemManager;
import me.lubomirstankov.gotCraftHub.manager.JumpManager;
import me.lubomirstankov.gotCraftHub.manager.PlayerDataManager;
import me.lubomirstankov.gotCraftHub.manager.SchematicDisplayManager;
import me.lubomirstankov.gotCraftHub.manager.SpawnManager;
import me.lubomirstankov.gotCraftHub.placeholder.GotCraftHubPlaceholder;
import me.lubomirstankov.gotCraftHub.task.DoubleJumpTask;
import me.lubomirstankov.gotCraftHub.task.HealthHungerTask;
import me.lubomirstankov.gotCraftHub.task.SchematicDisplayTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GotCraftHub extends JavaPlugin {
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private HubItemManager hubItemManager;
    private SpawnManager spawnManager;
    private SchematicDisplayManager schematicDisplayManager;
    private BungeeCordManager bungeeCordManager;
    private JumpManager jumpManager;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        playerDataManager = new PlayerDataManager();
        spawnManager = new SpawnManager(this);
        hubItemManager = new HubItemManager(this);
        jumpManager = new JumpManager(this);

        // Initialize schematic display manager (requires WorldEdit)
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            schematicDisplayManager = new SchematicDisplayManager(this);
            getLogger().info("WorldEdit integration enabled - schematic displays available!");
        } else {
            getLogger().warning("WorldEdit not found - schematic displays disabled");
        }

        // Initialize BungeeCord manager if enabled
        if (configManager.getConfig().getBoolean("bungeecord.enabled", false)) {
            bungeeCordManager = new BungeeCordManager(this);
        }

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        // Start tasks
        startTasks();

        // Register PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GotCraftHubPlaceholder(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        getLogger().info("GotCraftHub has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cleanup jump manager
        if (jumpManager != null) {
            jumpManager.cleanup();
        }

        // Cancel all tasks
        Bukkit.getScheduler().cancelTasks(this);

        // Disable BungeeCord manager
        if (bungeeCordManager != null) {
            bungeeCordManager.disable();
        }

        getLogger().info("GotCraftHub has been disabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new HubItemListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JumpListener(this), this);
        // Spawn listener handles void damage teleport back to hub spawn
        getServer().getPluginManager().registerEvents(new me.lubomirstankov.gotCraftHub.listener.SpawnListener(this), this);
        // Nametag listener disabled - caused duplicate names
        // getServer().getPluginManager().registerEvents(new NametagListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandFilterListener(this), this);
    }

    private void registerCommands() {
        GHubCommand ghubCommand = new GHubCommand(this);
        getCommand("ghub").setExecutor(ghubCommand);
        getCommand("ghub").setTabCompleter(ghubCommand);

        // Register blocked command handler for overridden Bukkit commands
        BlockedCommand blockedCommand = new BlockedCommand(this);
        getCommand("plugins").setExecutor(blockedCommand);
        getCommand("help").setExecutor(blockedCommand);
        getCommand("version").setExecutor(blockedCommand);

        // /spawn command
        getCommand("spawn").setExecutor(new SpawnCommand(this));
    }

    private void startTasks() {
        // Health and hunger task
        new HealthHungerTask(this).runTaskTimer(this, 0L, 20L);

        // Double jump enabler task
        new DoubleJumpTask(this).runTaskTimer(this, 0L, 5L);

        // Schematic display update task (every 5 seconds)
        if (schematicDisplayManager != null && schematicDisplayManager.isEnabled()) {
            int updateInterval = configManager.getConfig().getInt("schematic-display.update-interval", 100); // ticks
            new SchematicDisplayTask(this).runTaskTimer(this, 20L, updateInterval);
        }
    }

    // Getters
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public HubItemManager getHubItemManager() {
        return hubItemManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public SchematicDisplayManager getSchematicDisplayManager() {
        return schematicDisplayManager;
    }

    public BungeeCordManager getBungeeCordManager() {
        return bungeeCordManager;
    }

    public JumpManager getJumpManager() {
        return jumpManager;
    }
}
