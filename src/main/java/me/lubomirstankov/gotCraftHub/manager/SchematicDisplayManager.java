package me.lubomirstankov.gotCraftHub.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SchematicDisplayManager {
    private final GotCraftHub plugin;
    private final Map<Character, Clipboard> characterCache = new HashMap<>();
    private final File schematicsFolder;
    private Location displayLocation;
    private int characterSpacing;
    private int maxPlayers;

    public SchematicDisplayManager(GotCraftHub plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        // Create schematics folder if it doesn't exist
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }

        loadConfiguration();
        loadCharacters();
    }

    private void loadConfiguration() {
        // Load display location from config
        if (plugin.getConfig().contains("schematic-display.location")) {
            String worldName = plugin.getConfig().getString("schematic-display.location.world");
            double x = plugin.getConfig().getDouble("schematic-display.location.x");
            double y = plugin.getConfig().getDouble("schematic-display.location.y");
            double z = plugin.getConfig().getDouble("schematic-display.location.z");

            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                displayLocation = new Location(world, x, y, z);
            }
        }

        characterSpacing = plugin.getConfig().getInt("schematic-display.character-spacing", 5);
        maxPlayers = plugin.getConfig().getInt("schematic-display.max-players", 100);
    }

    private void loadCharacters() {
        // Load all number schematics (0-9)
        for (int i = 0; i <= 9; i++) {
            loadSchematic(String.valueOf(i).charAt(0), i + ".schem");
        }

        // Load slash for separating current/max players
        loadSchematic('/', "slash.schem");

        // Try loading "players.schem" first (contains "Players:" text)
        File playersFile = new File(schematicsFolder, "players.schem");
        if (playersFile.exists()) {
            loadSchematic('T', "players.schem"); // Load as 'T' to represent text "Players:"
            plugin.getLogger().info("Loaded players.schem for 'Players:' text");
        } else {
            // If players.schem doesn't exist, try individual letters
            loadSchematic('P', "p.schem");
            loadSchematic('l', "l.schem");
            loadSchematic('a', "a.schem");
            loadSchematic('y', "y.schem");
            loadSchematic('e', "e.schem");
            loadSchematic('r', "r.schem");
            loadSchematic('s', "s.schem");
            loadSchematic(':', "colon.schem");
        }

        plugin.getLogger().info("Loaded " + characterCache.size() + " schematic characters");
    }

    private void loadSchematic(char character, String filename) {
        File file = new File(schematicsFolder, filename);
        if (!file.exists()) {
            return;
        }

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                plugin.getLogger().warning("Unknown schematic format for: " + filename);
                return;
            }

            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                Clipboard clipboard = reader.read();
                characterCache.put(character, clipboard);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load schematic: " + filename, e);
        }
    }

    public void updateDisplay(int currentPlayers, int maxPlayers) {
        if (displayLocation == null) {
            plugin.getLogger().warning("Cannot update display - location not set! Use /ghub setdisplay first.");
            return;
        }

        plugin.getLogger().info("Updating schematic display at " + displayLocation + " with " + currentPlayers + "/" + maxPlayers);

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                clearDisplay();
                renderText(currentPlayers, maxPlayers);
                plugin.getLogger().info("Display update complete!");
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to update schematic display", e);
            }
        });
    }

    private void clearDisplay() {
        if (displayLocation == null) return;

        World world = displayLocation.getWorld();
        if (world == null) return;

        int clearWidth = plugin.getConfig().getInt("schematic-display.clear-width", 100);
        int clearHeight = plugin.getConfig().getInt("schematic-display.clear-height", 20);
        int clearDepth = plugin.getConfig().getInt("schematic-display.clear-depth", 10);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            BlockVector3 min = BlockVector3.at(
                displayLocation.getBlockX() - 5, // Add margin on left
                displayLocation.getBlockY() - 5, // Add margin below
                displayLocation.getBlockZ() - 5  // Add margin in front
            );
            BlockVector3 max = BlockVector3.at(
                displayLocation.getBlockX() + clearWidth,
                displayLocation.getBlockY() + clearHeight,
                displayLocation.getBlockZ() + clearDepth
            );

            com.sk89q.worldedit.regions.CuboidRegion region =
                new com.sk89q.worldedit.regions.CuboidRegion(
                    BukkitAdapter.adapt(world),
                    min,
                    max
                );

            // Clear the region by setting all blocks to air
            editSession.setBlocks(region, com.sk89q.worldedit.world.block.BlockTypes.AIR.getDefaultState());

            plugin.getLogger().info("Cleared display area from " + min + " to " + max);
        } catch (com.sk89q.worldedit.MaxChangedBlocksException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to clear display area (too many blocks changed)", e);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to clear display area", e);
        }
    }

    private void renderText(int currentPlayers, int maxPlayers) {
        if (displayLocation == null) return;

        int xOffset = 0;

        // Check if we have the pre-made "Players:" schematic (loaded as 'T')
        if (characterCache.containsKey('T')) {
            // Render "Players:" as one schematic
            xOffset += pasteSchematic('T', xOffset);
            plugin.getLogger().info("Rendering players.schem at offset " + xOffset);
        } else if (characterCache.containsKey('P')) {
            // Render "Players:" character by character
            String playersText = "Players:";
            for (int i = 0; i < playersText.length(); i++) {
                xOffset += pasteSchematic(playersText.charAt(i), xOffset);
            }
        }

        // Render the numbers: "2/6" for example
        String numbers = currentPlayers + "/" + maxPlayers;
        plugin.getLogger().info("Rendering numbers: " + numbers);
        for (int i = 0; i < numbers.length(); i++) {
            char c = numbers.charAt(i);
            xOffset += pasteSchematic(c, xOffset);
        }
    }

    private int pasteSchematic(char character, int xOffset) {
        Clipboard clipboard = characterCache.get(character);
        if (clipboard == null) {
            plugin.getLogger().warning("No schematic found for character: '" + character + "'");
            // Character not found, skip it with minimal spacing
            return characterSpacing / 2;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(
                BukkitAdapter.adapt(displayLocation.getWorld()))) {

            BlockVector3 pasteLocation = BlockVector3.at(
                displayLocation.getBlockX() + xOffset,
                displayLocation.getBlockY(),
                displayLocation.getBlockZ()
            );

            plugin.getLogger().info("Pasting '" + character + "' at " + pasteLocation);

            Operation operation = new ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(pasteLocation)
                .ignoreAirBlocks(false) // Don't ignore air blocks - this ensures clean replacement
                .build();

            Operations.complete(operation);

            // Return the width of this character + spacing
            int width = clipboard.getDimensions().getX();
            plugin.getLogger().info("Character '" + character + "' width: " + width + ", total offset: " + (width + characterSpacing));
            return width + characterSpacing;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to paste schematic for character: " + character, e);
            return characterSpacing;
        }
    }

    public void setDisplayLocation(Location location) {
        this.displayLocation = location;

        // Save to config
        plugin.getConfig().set("schematic-display.location.world", location.getWorld().getName());
        plugin.getConfig().set("schematic-display.location.x", location.getX());
        plugin.getConfig().set("schematic-display.location.y", location.getY());
        plugin.getConfig().set("schematic-display.location.z", location.getZ());
        plugin.saveConfig();
    }

    public Location getDisplayLocation() {
        return displayLocation;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("schematic-display.enabled", false);
    }

    public void reload() {
        characterCache.clear();
        loadConfiguration();
        loadCharacters();
    }
}

