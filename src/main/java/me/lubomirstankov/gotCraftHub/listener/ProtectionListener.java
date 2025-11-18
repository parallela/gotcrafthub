package me.lubomirstankov.gotCraftHub.listener;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class ProtectionListener implements Listener {
    private final GotCraftHub plugin;

    public ProtectionListener(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    private boolean isInHubWorld(Player player) {
        return player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld());
    }

    private boolean canBypass(Player player) {
        return plugin.getPlayerDataManager().isBuildModeEnabled(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isInHubWorld(player) && !canBypass(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("protection.block-break", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isInHubWorld(player) && !canBypass(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("protection.block-place", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isInHubWorld(player) && !canBypass(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("protection.interaction", true)) {
                if (event.getClickedBlock() != null) {
                    switch (event.getClickedBlock().getType()) {
                        case CHEST, TRAPPED_CHEST, BARREL, FURNACE, BLAST_FURNACE,
                             SMOKER, DISPENSER, DROPPER, HOPPER, BREWING_STAND,
                             ENCHANTING_TABLE, ANVIL, BEACON, CRAFTING_TABLE,
                             LECTERN, LOOM, STONECUTTER, GRINDSTONE, SMITHING_TABLE,
                             LEVER, STONE_BUTTON, OAK_BUTTON, SPRUCE_BUTTON,
                             BIRCH_BUTTON, JUNGLE_BUTTON, ACACIA_BUTTON,
                             DARK_OAK_BUTTON, CRIMSON_BUTTON, WARPED_BUTTON,
                             IRON_DOOR, OAK_DOOR, SPRUCE_DOOR, BIRCH_DOOR,
                             JUNGLE_DOOR, ACACIA_DOOR, DARK_OAK_DOOR,
                             CRIMSON_DOOR, WARPED_DOOR, IRON_TRAPDOOR,
                             OAK_TRAPDOOR, SPRUCE_TRAPDOOR, BIRCH_TRAPDOOR,
                             JUNGLE_TRAPDOOR, ACACIA_TRAPDOOR, DARK_OAK_TRAPDOOR,
                             CRIMSON_TRAPDOOR, WARPED_TRAPDOOR, OAK_FENCE_GATE,
                             SPRUCE_FENCE_GATE, BIRCH_FENCE_GATE, JUNGLE_FENCE_GATE,
                             ACACIA_FENCE_GATE, DARK_OAK_FENCE_GATE,
                             CRIMSON_FENCE_GATE, WARPED_FENCE_GATE -> {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isInHubWorld(player) && !canBypass(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("protection.item-drop", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        if (isInHubWorld(player) && !canBypass(player)) {
            if (plugin.getConfigManager().getConfig().getBoolean("protection.item-pickup", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isInHubWorld(player)) {
                if (plugin.getConfigManager().getConfig().getBoolean("protection.damage", true)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (isInHubWorld(victim)) {
                if (plugin.getConfigManager().getConfig().getBoolean("protection.pvp", true)) {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (isInHubWorld(attacker) && !canBypass(attacker)) {
                // Prevent armor stand and item frame damage
                if (event.getEntity().getType() == EntityType.ARMOR_STAND ||
                        event.getEntity().getType() == EntityType.ITEM_FRAME ||
                        event.getEntity().getType() == EntityType.GLOW_ITEM_FRAME) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isInHubWorld(player)) {
                if (!plugin.getConfigManager().getConfig().getBoolean("protection.hunger", false)) {
                    event.setCancelled(true);
                    player.setFoodLevel(20);
                    player.setSaturation(20);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {

            String hubWorld = plugin.getConfigManager().getHubWorld();
            if (event.getLocation().getWorld().getName().equalsIgnoreCase(hubWorld)) {
                if (plugin.getConfigManager().getConfig().getBoolean("protection.mob-spawns", true)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player) {
            if (isInHubWorld(player) && !canBypass(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event) {
        String hubWorld = plugin.getConfigManager().getHubWorld();
        if (event.getVehicle().getWorld().getName().equalsIgnoreCase(hubWorld)) {
            // Check if there's a player nearby who placed it
            event.getVehicle().getWorld().getNearbyEntities(event.getVehicle().getLocation(), 5, 5, 5).stream()
                    .filter(entity -> entity instanceof Player)
                    .findFirst()
                    .ifPresent(entity -> {
                        Player player = (Player) entity;
                        if (!canBypass(player)) {
                            event.getVehicle().remove();
                        }
                    });
        }
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (isInHubWorld(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}

