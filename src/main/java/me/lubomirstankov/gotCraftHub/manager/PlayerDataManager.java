package me.lubomirstankov.gotCraftHub.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerDataManager {
    private final Set<UUID> buildMode = new HashSet<>();
    private final Set<UUID> playersHidden = new HashSet<>();
    private final Map<UUID, Long> doubleJumpCooldown = new HashMap<>();
    private final Map<UUID, Long> pressurePlateCooldown = new HashMap<>();

    public boolean isBuildModeEnabled(Player player) {
        return buildMode.contains(player.getUniqueId());
    }

    public void setBuildMode(Player player, boolean enabled) {
        if (enabled) {
            buildMode.add(player.getUniqueId());
        } else {
            buildMode.remove(player.getUniqueId());
        }
    }

    public boolean arePlayersHidden(Player player) {
        return playersHidden.contains(player.getUniqueId());
    }

    public void setPlayersHidden(Player player, boolean hidden) {
        if (hidden) {
            playersHidden.add(player.getUniqueId());
        } else {
            playersHidden.remove(player.getUniqueId());
        }
    }

    public boolean isDoubleJumpOnCooldown(Player player, double cooldownSeconds) {
        UUID uuid = player.getUniqueId();
        if (doubleJumpCooldown.containsKey(uuid)) {
            long lastUse = doubleJumpCooldown.get(uuid);
            long cooldownMs = (long) (cooldownSeconds * 1000);
            return System.currentTimeMillis() - lastUse < cooldownMs;
        }
        return false;
    }

    public void setDoubleJumpCooldown(Player player) {
        doubleJumpCooldown.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isPressurePlateOnCooldown(Player player, double cooldownSeconds) {
        UUID uuid = player.getUniqueId();
        if (pressurePlateCooldown.containsKey(uuid)) {
            long lastUse = pressurePlateCooldown.get(uuid);
            long cooldownMs = (long) (cooldownSeconds * 1000);
            return System.currentTimeMillis() - lastUse < cooldownMs;
        }
        return false;
    }

    public void setPressurePlateCooldown(Player player) {
        pressurePlateCooldown.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void clearPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        buildMode.remove(uuid);
        playersHidden.remove(uuid);
        doubleJumpCooldown.remove(uuid);
        pressurePlateCooldown.remove(uuid);
    }

    public int getVisiblePlayersCount(Player player) {
        if (arePlayersHidden(player)) {
            return 0;
        }
        return (int) player.getWorld().getPlayers().stream()
                .filter(p -> !p.equals(player))
                .count();
    }
}

