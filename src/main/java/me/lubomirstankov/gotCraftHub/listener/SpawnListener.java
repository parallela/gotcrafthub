package me.lubomirstankov.gotCraftHub.listener;

import me.lubomirstankov.gotCraftHub.GotCraftHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class SpawnListener implements Listener {
    private final GotCraftHub plugin;

    public SpawnListener(GotCraftHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;

        Player player = (Player) event.getEntity();

        // Only handle void teleport for players in the configured hub world
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getConfigManager().getHubWorld())) {
            return;
        }

        // Cancel the damage and teleport to configured spawn (or world spawn as fallback)
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location target = null;
            if (plugin.getSpawnManager().hasSpawn()) {
                target = plugin.getSpawnManager().getSpawn();
            } else {
                World world = Bukkit.getWorld(plugin.getConfigManager().getHubWorld());
                if (world != null) target = world.getSpawnLocation();
            }

            if (target != null) {
                // ensure we don't immediately fall through blocks; teleport to exact spawn
                player.teleport(target);
            }
        });
    }
}

