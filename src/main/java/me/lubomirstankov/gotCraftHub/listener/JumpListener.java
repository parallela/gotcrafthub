package me.lubomirstankov.gotCraftHub.listener;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JumpListener implements Listener {
    private final GotCraftHub plugin;

    public JumpListener(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Check if player is in jump game
        if (!plugin.getJumpManager().isPlayerInGame(player)) {
            return;
        }

        // Check if player moved from one block to another
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        // Check if player is on ground
        if (player.isOnGround()) {
            Block blockBelow = to.getBlock().getRelative(0, -1, 0);
            plugin.getJumpManager().handleBlockLand(player, blockBelow);
        }

        // Check if player fell below the arena
        if (plugin.getJumpManager().getPos1() != null && plugin.getJumpManager().getPos2() != null) {
            int minY = Math.min(
                plugin.getJumpManager().getPos1().getBlockY(),
                plugin.getJumpManager().getPos2().getBlockY()
            );

            // If player falls below minimum Y - 10, end session
            if (to.getY() < minY - 10) {
                plugin.getJumpManager().endJumpSession(player, true);
                player.sendMessage(plugin.getConfigManager().getMessage("jump-fell"));
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Prevent damage in jump game
        if (plugin.getJumpManager().isPlayerInGame(player)) {
            event.setCancelled(true);

            // If void damage, end session
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                plugin.getJumpManager().endJumpSession(player, true);
                player.sendMessage(plugin.getConfigManager().getMessage("jump-fell"));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // End jump session if player quits
        if (plugin.getJumpManager().isPlayerInGame(player)) {
            plugin.getJumpManager().endJumpSession(player, false);
        }
    }
}

