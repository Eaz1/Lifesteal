package com.example.customenchant;

import org.bukkit.entity.Player;

public final class TemporaryHealthAPI {

    private static TemporaryHealthManager manager;

    private TemporaryHealthAPI() {}

    protected static void register(TemporaryHealthManager m) {
        manager = m;
    }

    /**
     * Adds temporary maximum health to a player from a specific source.
     */
    public static void addTemporaryHealth(Player player, double amount, String source) {
        if (manager != null) {
            manager.addTemporaryHealth(player, amount, source);
        }
    }

    /**
     * Removes temporary maximum health added by a specific source.
     */
    public static void removeTemporaryHealth(Player player, String source) {
        if (manager != null) {
            manager.removeTemporaryHealth(player, source);
        }
    }

    /**
     * Gets the current combined total of all active temporary health modifiers.
     */
    public static double getTemporaryHealth(Player player) {
        return manager != null ? manager.getTemporaryHealth(player) : 0.0;
    }

    /**
     * Gets the base permanent health of the player (owned by GalacticLifeSteal/Vanilla).
     */
    public static double getPermanentHealth(Player player) {
        return manager != null ? manager.getPermanentHealth(player) : 0.0;
    }

    /**
     * Gets the total overall maximum health (Permanent + Temporary modifiers).
     */
    public static double getTotalHealth(Player player) {
        return manager != null ? manager.getTotalHealth(player) : 0.0;
    }
}
