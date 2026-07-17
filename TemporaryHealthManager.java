package com.example.customenchant;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TemporaryHealthManager {

    private static final String MODIFIER_PREFIX = "TempHealth_";

    /**
     * Generates a deterministic UUID based on the player and source.
     * This guarantees that even across reloads and restarts, the system can locate and strip the exact modifier.
     */
    private UUID getDeterministicUUID(UUID playerUuid, String source) {
        String key = MODIFIER_PREFIX + playerUuid.toString() + "_" + source;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    public void addTemporaryHealth(Player player, double amount, String source) {
        if (player == null || source == null || amount <= 0) return;

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;

        UUID modifierUuid = getDeterministicUUID(player.getUniqueId(), source);

        // Remove the modifier if it already exists to prevent duplicate stacking
        removeModifierIfExists(maxHealthAttr, modifierUuid);

        // Create and apply the modifier
        AttributeModifier modifier = new AttributeModifier(
                modifierUuid,
                MODIFIER_PREFIX + source,
                amount,
                AttributeModifier.Operation.ADD_NUMBER
        );

        maxHealthAttr.addModifier(modifier);
    }

    public void removeTemporaryHealth(Player player, String source) {
        if (player == null || source == null) return;

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;

        UUID modifierUuid = getDeterministicUUID(player.getUniqueId(), source);
        removeModifierIfExists(maxHealthAttr, modifierUuid);

        // If their current health exceeds their new maximum, clamp it safely
        double currentMax = maxHealthAttr.getValue();
        if (player.getHealth() > currentMax) {
            player.setHealth(currentMax);
        }
    }

    public double getTemporaryHealth(Player player) {
        if (player == null) return 0.0;
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return 0.0;

        double tempHealth = 0.0;
        for (AttributeModifier modifier : maxHealthAttr.getModifiers()) {
            if (modifier.getName().startsWith(MODIFIER_PREFIX)) {
                tempHealth += modifier.getAmount();
            }
        }
        return tempHealth;
    }

    public double getPermanentHealth(Player player) {
        if (player == null) return 0.0;
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return 0.0;

        // Permanent health is the clean base value, managed by GalacticLifeSteal or vanilla systems
        return maxHealthAttr.getBaseValue();
    }

    public double getTotalHealth(Player player) {
        if (player == null) return 0.0;
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return 0.0;

        return maxHealthAttr.getValue();
    }

    /**
     * Completely strips all temporary modifiers from a player.
     * Crucial during disconnects and server restarts.
     */
    public void clearAllTemporaryModifiers(Player player) {
        if (player == null) return;
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;

        List<AttributeModifier> toRemove = new ArrayList<>();
        for (AttributeModifier modifier : maxHealthAttr.getModifiers()) {
            if (modifier.getName().startsWith(MODIFIER_PREFIX)) {
                toRemove.add(modifier);
            }
        }

        for (AttributeModifier modifier : toRemove) {
            maxHealthAttr.removeModifier(modifier);
        }

        double currentMax = maxHealthAttr.getValue();
        if (player.getHealth() > currentMax) {
            player.setHealth(currentMax);
        }
    }

    private void removeModifierIfExists(AttributeInstance attributeInstance, UUID uuid) {
        for (AttributeModifier modifier : attributeInstance.getModifiers()) {
            if (modifier.getUniqueId().equals(uuid)) {
                attributeInstance.removeModifier(modifier);
            }
        }
    }
}
