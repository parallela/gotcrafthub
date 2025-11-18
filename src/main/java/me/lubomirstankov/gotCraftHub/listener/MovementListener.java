package me.lubomirstankov.gotCraftHub.listener;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

public class MovementListener implements Listener {
    private final GotCraftHub plugin;

    public MovementListener(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        // Check for pressure plate boost
        if (plugin.getConfigManager().isPressurePlateBoostEnabled()) {
            Material blockType = player.getLocation().getBlock().getType();

            if (blockType == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
                blockType == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {

                if (!plugin.getPlayerDataManager().isPressurePlateOnCooldown(player,
                        plugin.getConfigManager().getPressurePlateCooldown())) {

                    boostPlayer(player);
                    plugin.getPlayerDataManager().setPressurePlateCooldown(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        if (!plugin.getConfigManager().isDoubleJumpEnabled()) {
            return;
        }

        // This is triggered when player double-taps space
        if (!player.isFlying()) {
            event.setCancelled(true);

            if (!plugin.getPlayerDataManager().isDoubleJumpOnCooldown(player,
                    plugin.getConfigManager().getDoubleJumpCooldown())) {

                doubleJump(player);
                plugin.getPlayerDataManager().setDoubleJumpCooldown(player);

                // Allow flying briefly to enable double jump detection
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
    }

    private void boostPlayer(Player player) {
        double horizontal = plugin.getConfigManager().getPressurePlateHorizontal();
        double vertical = plugin.getConfigManager().getPressurePlateVertical();

        Vector direction = player.getLocation().getDirection();
        direction.setY(0).normalize();
        direction.multiply(horizontal);
        direction.setY(vertical);

        player.setVelocity(direction);
    }

    private void doubleJump(Player player) {
        double power = plugin.getConfigManager().getDoubleJumpPower();

        Vector direction = player.getLocation().getDirection();
        direction.setY(0).normalize();
        direction.multiply(power * 0.8);
        direction.setY(power * 0.5);

        player.setVelocity(player.getVelocity().add(direction));
    }
}

