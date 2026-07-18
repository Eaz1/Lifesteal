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

        // Lifesteal Compatibility Detection
        boolean glsPresent = Bukkit.getPluginManager().getPlugin("GalacticLifeSteal") != null;
        boolean heartstealPresent = Bukkit.getPluginManager().getPlugin("Heartsteal") != null;

        if (glsPresent) {
            getLogger().info("GalacticLifeSteal detected! Seamless heart compatibility active.");
        } else if (heartstealPresent) {
            getLogger().info("Heartsteal detected! TemporaryHealth compatibility active.");
        } else {
            getLogger().info("No compatible Lifesteal plugin detected. Operating in standalone mode.");
        }

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(
                new OverloadListener(this, overloadManager, healthManager),
                this
        );

        // Periodic Sanity Check Task
        // Runs every second to keep temporary hearts synchronized
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                overloadManager.updateOverload(player);
            }
        }, 20L, 20L);

        getLogger().info("TemporaryHealth v" + getDescription().getVersion() + " enabled successfully.");
    }

    @Override
    public void onDisable() {

        // Remove all temporary modifiers before player data is saved
        if (healthManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                healthManager.clearAllTemporaryModifiers(player);
            }
        }

        getLogger().info("TemporaryHealth disabled.");
    }

    public TemporaryHealthManager getHealthManager() {
        return healthManager;
    }

    public OverloadManager getOverloadManager() {
        return overloadManager;
    }
}
