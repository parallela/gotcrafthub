package me.lubomirstankov.gotCraftHub.manager;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.lubomirstankov.gotCraftHub.GotCraftHub;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class HubItemManager {
    private final GotCraftHub plugin;
    private final Map<Integer, HubItem> hubItems = new HashMap<>();
    private final MiniMessage miniMessage;

    public HubItemManager(GotCraftHub plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadHubItems();
    }

    public void loadHubItems() {
        hubItems.clear();
        ConfigurationSection itemsSection = plugin.getConfigManager().getHubItemsConfig().getConfigurationSection("items");

        if (itemsSection == null) {
            plugin.getLogger().warning("No items found in hub-items.yml");
            return;
        }

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            int slot = itemSection.getInt("slot", 0);
            String materialName = itemSection.getString("material", "STONE");
            String name = itemSection.getString("name", "");
            List<String> lore = itemSection.getStringList("lore");
            String command = itemSection.getString("command", "");
            String texture = itemSection.getString("texture", "");
            boolean playerSkin = itemSection.getBoolean("player-skin", false);

            Material material;
            try {
                material = Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material: " + materialName + " for item " + key);
                continue;
            }

            HubItem hubItem = new HubItem(slot, material, name, lore, command, texture, playerSkin);
            hubItems.put(slot, hubItem);
        }

        plugin.getLogger().info("Loaded " + hubItems.size() + " hub items");
    }

    public void giveHubItems(Player player) {
        boolean clearInventory = plugin.getConfigManager().getConfig().getBoolean("hub-items.clear-inventory", true);

        if (clearInventory) {
            player.getInventory().clear();
        }

        for (HubItem hubItem : hubItems.values()) {
            ItemStack item = createItemStack(hubItem, player);
            player.getInventory().setItem(hubItem.slot, item);
        }

        player.updateInventory();
    }

    private ItemStack createItemStack(HubItem hubItem, Player player) {
        ItemStack item = new ItemStack(hubItem.material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Replace player placeholders in name
            String displayName = hubItem.name.replace("%player_name%", player.getName());
            meta.setDisplayName(colorize(displayName));

            if (!hubItem.lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : hubItem.lore) {
                    // Replace player placeholders in lore
                    String processedLine = line.replace("%player_name%", player.getName());
                    coloredLore.add(colorize(processedLine));
                }
                meta.setLore(coloredLore);
            }

            // Handle skull texture
            if (hubItem.material == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
                if (hubItem.playerSkin) {
                    // Use player's own skin
                    skullMeta.setOwningPlayer(player);
                } else if (!hubItem.texture.isEmpty()) {
                    // Use custom base64 texture
                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.setProperty(new ProfileProperty("textures", hubItem.texture));
                    skullMeta.setPlayerProfile(profile);
                }
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public HubItem getHubItem(int slot) {
        return hubItems.get(slot);
    }

    public boolean isHubItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        // Check if the item matches any hub item by slot and material
        for (HubItem hubItem : hubItems.values()) {
            if (item.getType() == hubItem.material) {
                // For items with display names, check if it has a display name
                if (item.getItemMeta().hasDisplayName()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String colorize(String text) {
        try {
            // Try MiniMessage format first, convert to legacy for ItemMeta
            return LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(text));
        } catch (Exception e) {
            // Fallback to legacy color codes (&)
            return text.replace("&", "§");
        }
    }

    public static class HubItem {
        public final int slot;
        public final Material material;
        public final String name;
        public final List<String> lore;
        public final String command;
        public final String texture;
        public final boolean playerSkin;

        public HubItem(int slot, Material material, String name, List<String> lore, String command, String texture, boolean playerSkin) {
            this.slot = slot;
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.command = command;
            this.texture = texture;
            this.playerSkin = playerSkin;
        }
    }
}
