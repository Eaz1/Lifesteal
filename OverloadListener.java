package com.example.customenchant;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;

public class OverloadListener implements Listener {

    private final TemporaryHealthPlugin plugin;
    private final OverloadManager overloadManager;
    private final TemporaryHealthManager healthManager;

    public OverloadListener(TemporaryHealthPlugin plugin, OverloadManager overloadManager, TemporaryHealthManager healthManager) {
        this.plugin = plugin;
        this.overloadManager = overloadManager;
        this.healthManager = healthManager;
    }

    /**
     * Executes the recalculation on the next tick. 
     * This avoids conflicts with Minecraft's click-handling phases.
     */
    private void scheduleUpdate(Player player) {
        if (player == null) return;
        Bukkit.getScheduler().runTask(plugin, () -> overloadManager.updateOverload(player));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            scheduleUpdate((Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            scheduleUpdate((Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Catches right-clicking armor from the hotbar to quick-equip
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemBreak(EntityDamageEvent event) {
        // Recalculates if an armor piece shatters from taking damage
        if (event.getEntity() instanceof Player) {
            scheduleUpdate((Player) event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Clear any old modifiers in case of a server crash
        healthManager.clearAllTemporaryModifiers(event.getPlayer());
        // Apply fresh calculation
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Strip everything so temporary attributes are never written to the player's DAT file
        healthManager.clearAllTemporaryModifiers(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        healthManager.clearAllTemporaryModifiers(event.getPlayer());
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        scheduleUpdate(event.getPlayer());
    }
}
