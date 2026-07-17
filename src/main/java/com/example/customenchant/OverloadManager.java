package com.example.customenchant;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OverloadManager {

    private final TemporaryHealthPlugin plugin;
    private final TemporaryHealthManager healthManager;
    private static final String OVERLOAD_SOURCE = "Overload";

    public OverloadManager(TemporaryHealthPlugin plugin, TemporaryHealthManager healthManager) {
        this.plugin = plugin;
        this.healthManager = healthManager;
    }

    /**
     * Evaluates the player's equipped armor and applies/removes temporary health.
     */
    public void updateOverload(Player player) {
        if (player == null || !player.isOnline()) return;

        int totalLevel = 0;
        ItemStack[] armor = player.getInventory().getArmorContents();
        
        if (armor != null) {
            for (ItemStack item : armor) {
                if (item != null && item.getType() != Material.AIR) {
                    totalLevel += getOverloadLevel(item);
                }
            }
        }

        if (totalLevel > 0) {
            // Formula: Each level of Overload across all armor slots adds +2.0 HP (1 Heart)
            double bonusHealth = totalLevel * 2.0; 
            healthManager.addTemporaryHealth(player, bonusHealth, OVERLOAD_SOURCE);
        } else {
            healthManager.removeTemporaryHealth(player, OVERLOAD_SOURCE);
        }
    }

    private int getOverloadLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return 0;

        for (String line : item.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line).toLowerCase();
            if (stripped.contains("overload")) {
                return parseRomanOrDigit(stripped.replace("overload", "").trim());
            }
        }
        return 0;
    }

    private int parseRomanOrDigit(String str) {
        if (str.isEmpty()) return 1;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            switch (str.toUpperCase()) {
                case "I": return 1;
                case "II": return 2;
                case "III": return 3;
                case "IV": return 4;
                case "V": return 5;
                default: return 1;
            }
        }
    }
}
