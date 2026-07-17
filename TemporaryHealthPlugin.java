package com.example.customenchant;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TemporaryHealthPlugin extends JavaPlugin {

    private TemporaryHealthManager healthManager;
    private OverloadManager overloadManager;

    @Override
    public void onEnable() {
        // Initialize Core System
        this.healthManager = new TemporaryHealthManager();
        this.overloadManager = new OverloadManager(this, healthManager);

        // Register public API interface
        TemporaryHealthAPI.register(this.healthManager);

        // GalacticLifeSteal Detection
        boolean glsPresent = Bukkit.getPluginManager().getPlugin("GalacticLifeSteal") != null;
        if (glsPresent) {
            getLogger().info("GalacticLifeSteal detected! Seamless heart compatibility active.");
        } else {
            getLogger().info("GalacticLifeSteal not detected. Operating in standalone mode.");
        }

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(
            new OverloadListener(this, overloadManager, healthManager), 
            this
        );

        // Periodic Sanity Check Task (Runs every 20 ticks / 1 second)
        // This handles cases like armor breaking, inventory manipulations, chestplate auto-equips, and lag exploits.
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                overloadManager.updateOverload(player);
            }
        }, 20L, 20L);
    }

    @Override
    public void onDisable() {
        // Essential: Strip all temporary health modifiers from online players 
        // BEFORE the server saves player files. This prevents temporary hearts from saving to DAT files.
        if (healthManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                healthManager.clearAllTemporaryModifiers(player);
            }
        }
    }

    public TemporaryHealthManager getHealthManager() {
        return healthManager;
    }

    public OverloadManager getOverloadManager() {
        return overloadManager;
    }
}
