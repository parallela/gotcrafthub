package me.lubomirstankov.gotCraftHub.manager;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JumpManager {
    private final GotCraftHub plugin;
    private Location pos1;
    private Location pos2;
    private Location jumpRespawn;

    // Active players in jump game
    private final Map<UUID, JumpSession> activeSessions = new ConcurrentHashMap<>();

    // Leaderboard: player UUID -> best score
    private final Map<UUID, Integer> leaderboard = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerNames = new ConcurrentHashMap<>();

    // Current blocks for each session
    private final Map<UUID, Location> currentBlocks = new ConcurrentHashMap<>();

    private final Random random = new Random();
    private final int MAX_PLAYERS = 5;

    private File dataFile;
    private FileConfiguration data;

    public JumpManager(GotCraftHub plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "jump-data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create jump-data.yml!");
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        // Load positions
        if (data.contains("pos1")) {
            pos1 = (Location) data.get("pos1");
        }
        if (data.contains("pos2")) {
            pos2 = (Location) data.get("pos2");
        }
        if (data.contains("jump-respawn")) {
            jumpRespawn = (Location) data.get("jump-respawn");
        }

        // Load leaderboard
        if (data.contains("leaderboard")) {
            for (String key : data.getConfigurationSection("leaderboard").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                int score = data.getInt("leaderboard." + key + ".score");
                String name = data.getString("leaderboard." + key + ".name");
                leaderboard.put(uuid, score);
                playerNames.put(uuid, name);
            }
        }
    }

    public void saveData() {
        if (pos1 != null) {
            data.set("pos1", pos1);
        }
        if (pos2 != null) {
            data.set("pos2", pos2);
        }
        if (jumpRespawn != null) {
            data.set("jump-respawn", jumpRespawn);
        }

        // Save leaderboard
        for (Map.Entry<UUID, Integer> entry : leaderboard.entrySet()) {
            String key = entry.getKey().toString();
            data.set("leaderboard." + key + ".score", entry.getValue());
            data.set("leaderboard." + key + ".name", playerNames.get(entry.getKey()));
        }

        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save jump-data.yml!");
        }
    }

    public void setPos1(Location location) {
        this.pos1 = location;
        saveData();
    }

    public void setPos2(Location location) {
        this.pos2 = location;
        saveData();
    }

    public void setJumpRespawn(Location location) {
        this.jumpRespawn = location;
        saveData();
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public Location getJumpRespawn() {
        return jumpRespawn;
    }

    public boolean isSetupComplete() {
        return pos1 != null && pos2 != null && jumpRespawn != null;
    }

    public boolean canJoinGame() {
        return activeSessions.size() < MAX_PLAYERS;
    }

    public boolean isPlayerInGame(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public int getActivePlayerCount() {
        return activeSessions.size();
    }

    /**
     * Start a jump session for a player
     */
    public boolean startJumpSession(Player player) {
        if (!isSetupComplete()) {
            return false;
        }

        if (activeSessions.size() >= MAX_PLAYERS) {
            return false;
        }

        // Calculate spawn position based on number of active players
        Location spawnLocation = calculatePlayerSpawnLocation(activeSessions.size());

        JumpSession session = new JumpSession(player.getUniqueId(), spawnLocation);
        activeSessions.put(player.getUniqueId(), session);

        // Teleport player
        player.teleport(spawnLocation);

        // Spawn first block
        spawnNextBlock(player);

        return true;
    }

    /**
     * Calculate spawn location to separate players
     */
    private Location calculatePlayerSpawnLocation(int playerIndex) {
        if (pos1 == null || pos2 == null) return null;

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int width = maxX - minX;
        int depth = maxZ - minZ;

        // Divide area into grid based on max players
        int gridSize = (int) Math.ceil(Math.sqrt(MAX_PLAYERS));
        int gridX = playerIndex % gridSize;
        int gridZ = playerIndex / gridSize;

        int cellWidth = width / gridSize;
        int cellDepth = depth / gridSize;

        int spawnX = minX + (gridX * cellWidth) + (cellWidth / 2);
        int spawnZ = minZ + (gridZ * cellDepth) + (cellDepth / 2);
        int spawnY = Math.max(pos1.getBlockY(), pos2.getBlockY()) + 1;

        return new Location(pos1.getWorld(), spawnX + 0.5, spawnY, spawnZ + 0.5);
    }

    /**
     * Spawn the next random block for a player
     */
    public void spawnNextBlock(Player player) {
        UUID uuid = player.getUniqueId();
        JumpSession session = activeSessions.get(uuid);
        if (session == null) return;

        // Clear previous block if exists
        Location previousBlock = currentBlocks.get(uuid);
        if (previousBlock != null) {
            previousBlock.getBlock().setType(Material.AIR);
        }

        // Get player's area bounds
        Location spawnLoc = session.getSpawnLocation();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());

        // Calculate player's subdivision area
        int width = maxX - minX;
        int depth = maxZ - minZ;
        int gridSize = (int) Math.ceil(Math.sqrt(MAX_PLAYERS));
        int cellWidth = width / gridSize;
        int cellDepth = depth / gridSize;

        int playerIndex = new ArrayList<>(activeSessions.keySet()).indexOf(uuid);
        int gridX = playerIndex % gridSize;
        int gridZ = playerIndex / gridSize;

        int cellMinX = minX + (gridX * cellWidth);
        int cellMaxX = minX + ((gridX + 1) * cellWidth);
        int cellMinZ = minZ + (gridZ * cellDepth);
        int cellMaxZ = minZ + ((gridZ + 1) * cellDepth);

        // Random location within player's cell
        int randomX = cellMinX + random.nextInt(Math.max(1, cellMaxX - cellMinX));
        int randomZ = cellMinZ + random.nextInt(Math.max(1, cellMaxZ - cellMinZ));
        int randomY = minY + random.nextInt(Math.max(1, maxY - minY + 1));

        Location blockLocation = new Location(pos1.getWorld(), randomX, randomY, randomZ);

        // Get block type from config
        String blockTypeName = plugin.getConfigManager().getConfig().getString("jump.block-type", "SLIME_BLOCK");
        Material blockType;
        try {
            blockType = Material.valueOf(blockTypeName);
        } catch (IllegalArgumentException e) {
            blockType = Material.SLIME_BLOCK;
        }

        blockLocation.getBlock().setType(blockType);
        currentBlocks.put(uuid, blockLocation);
    }

    /**
     * Handle player landing on a block
     */
    public void handleBlockLand(Player player, Block block) {
        UUID uuid = player.getUniqueId();
        Location currentBlock = currentBlocks.get(uuid);

        if (currentBlock != null && currentBlock.equals(block.getLocation())) {
            JumpSession session = activeSessions.get(uuid);
            if (session != null) {
                session.incrementScore();

                // Send score message
                player.sendMessage(plugin.getConfigManager().getMessage("jump-score")
                    .replaceText(builder -> builder.matchLiteral("%score%").replacement(String.valueOf(session.getScore()))));

                // Spawn next block
                spawnNextBlock(player);
            }
        }
    }

    /**
     * End a player's jump session
     */
    public void endJumpSession(Player player, boolean teleportToRespawn) {
        UUID uuid = player.getUniqueId();
        JumpSession session = activeSessions.remove(uuid);

        if (session != null) {
            // Clear current block
            Location currentBlock = currentBlocks.remove(uuid);
            if (currentBlock != null) {
                currentBlock.getBlock().setType(Material.AIR);
            }

            // Update leaderboard
            int score = session.getScore();
            if (score > 0) {
                int currentBest = leaderboard.getOrDefault(uuid, 0);
                if (score > currentBest) {
                    leaderboard.put(uuid, score);
                    playerNames.put(uuid, player.getName());
                    saveData();

                    // Notify player of new record
                    player.sendMessage(plugin.getConfigManager().getMessage("jump-new-record")
                        .replaceText(builder -> builder.matchLiteral("%score%").replacement(String.valueOf(score))));
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("jump-final-score")
                        .replaceText(builder -> builder.matchLiteral("%score%").replacement(String.valueOf(score))));
                }
            }

            // Teleport to respawn
            if (teleportToRespawn && jumpRespawn != null) {
                player.teleport(jumpRespawn);
            }
        }
    }

    /**
     * Get top players for leaderboard
     */
    public List<Map.Entry<String, Integer>> getTopPlayers(int limit) {
        List<Map.Entry<String, Integer>> topList = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : leaderboard.entrySet()) {
            String name = playerNames.getOrDefault(entry.getKey(), "Unknown");
            topList.add(new AbstractMap.SimpleEntry<>(name, entry.getValue()));
        }

        topList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        return topList.subList(0, Math.min(limit, topList.size()));
    }

    /**
     * Get player's rank (1-based)
     */
    public int getPlayerRank(UUID uuid) {
        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(leaderboard.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Get player's best score
     */
    public int getPlayerScore(UUID uuid) {
        return leaderboard.getOrDefault(uuid, 0);
    }

    /**
     * Cleanup when plugin disables
     */
    public void cleanup() {
        // End all active sessions
        for (UUID uuid : new ArrayList<>(activeSessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                endJumpSession(player, true);
            }
        }

        // Clear all blocks
        for (Location loc : currentBlocks.values()) {
            if (loc != null) {
                loc.getBlock().setType(Material.AIR);
            }
        }

        activeSessions.clear();
        currentBlocks.clear();
        saveData();
    }

    /**
     * Jump session data
     */
    private static class JumpSession {
        private final UUID playerUuid;
        private final Location spawnLocation;
        private int score;
        private final long startTime;

        public JumpSession(UUID playerUuid, Location spawnLocation) {
            this.playerUuid = playerUuid;
            this.spawnLocation = spawnLocation;
            this.score = 0;
            this.startTime = System.currentTimeMillis();
        }

        public void incrementScore() {
            score++;
        }

        public int getScore() {
            return score;
        }

        public Location getSpawnLocation() {
            return spawnLocation;
        }

        public UUID getPlayerUuid() {
            return playerUuid;
        }
    }
}

