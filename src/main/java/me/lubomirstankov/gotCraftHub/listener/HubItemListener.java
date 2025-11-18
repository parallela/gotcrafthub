package me.lubomirstankov.gotCraftHub.listener;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import me.lubomirstankov.gotCraftHub.manager.HubItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class HubItemListener implements Listener {
    private final GotCraftHub plugin;

    public HubItemListener(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        if (!plugin.getConfigManager().getConfig().getBoolean("hub-items.enabled", true)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !plugin.getHubItemManager().isHubItem(item)) {
            return;
        }

        // Cancel the event to prevent item duplication
        event.setCancelled(true);

        // Prevent the item from being used
        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);

        // Find which hub item was clicked
        int slot = player.getInventory().getHeldItemSlot();
        HubItemManager.HubItem hubItem = plugin.getHubItemManager().getHubItem(slot);

        if (hubItem == null) {
            // Try to find by matching item
            for (int i = 0; i < 9; i++) {
                ItemStack slotItem = player.getInventory().getItem(i);
                if (slotItem != null && slotItem.isSimilar(item)) {
                    hubItem = plugin.getHubItemManager().getHubItem(i);
                    break;
                }
            }
        }

        if (hubItem != null && !hubItem.command.isEmpty()) {
            executeCommand(player, hubItem.command);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        if (!plugin.getConfigManager().getConfig().getBoolean("hub-items.enabled", true)) {
            return;
        }

        if (plugin.getPlayerDataManager().isBuildModeEnabled(player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item != null && plugin.getHubItemManager().isHubItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        if (!plugin.getConfigManager().getConfig().getBoolean("hub-items.enabled", true)) {
            return;
        }

        if (plugin.getPlayerDataManager().isBuildModeEnabled(player)) {
            return;
        }

        if (plugin.getHubItemManager().isHubItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    private void executeCommand(Player player, String command) {
        // Replace placeholders
        String processedCommand = command
                .replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId().toString());

        // Schedule command execution for next tick to avoid conflicts
        Bukkit.getScheduler().runTask(plugin, () -> {
            // Check if command should be run as console
            if (processedCommand.startsWith("console:")) {
                String consoleCommand = processedCommand.substring(8).trim();
                // Remove leading slash if present
                if (consoleCommand.startsWith("/")) {
                    consoleCommand = consoleCommand.substring(1);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
            } else {
                // Remove leading slash if present for player commands
                String playerCommand = processedCommand.trim();
                if (playerCommand.startsWith("/")) {
                    playerCommand = playerCommand.substring(1);
                }
                // Use dispatchCommand instead of performCommand for better compatibility
                Bukkit.dispatchCommand(player, playerCommand);
            }
        });
    }
}
