package com.shard.generalswap.util;


import com.shard.generalswap.state.PlayerState;
import com.shard.generalswap.util.BukkitCompat;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.CompassMeta;

// Use fully-qualified reference for BukkitCompat to avoid IDE false positives

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for capturing and applying player states
 */
public class PlayerStateUtil {

    /**
     * Capture a player's current state
     * @param player The player to capture state from
     * @return The captured player state
     */

    public static PlayerState capturePlayerState(Player player) {

        List<ItemStack> overflow = new ArrayList<>();
        player.closeInventory();
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            vehicle.removePassenger(player);
        }

        return new PlayerState(
                player.getInventory().getContents().clone(),
                player.getInventory().getArmorContents().clone(),
                player.getInventory().getItemInOffHand().clone(),
                player.getLocation().clone(),
                player.getHealth(),
                player.getFoodLevel(),
                player.getSaturation(),
                player.getExhaustion(),
                player.getTotalExperience(),
                player.getExp(),
                player.getLevel(),
                player.getFireTicks(),
                player.getRemainingAir(),
                player.getMaximumAir(),
                player.getGameMode(),
                player.getFallDistance(),
                player.getAllowFlight(),
                player.isFlying(),
                new ArrayList<>(player.getActivePotionEffects()),
                player.getAbsorptionAmount(),
                vehicle,
                player.isInsideVehicle(),
                player.getTicksLived(),
                player.getLastDamage(),
                player.getNoDamageTicks(),
                player.isGliding(),
                player.getWalkSpeed(),
                player.getFlySpeed(),
                player.getPortalCooldown(),
                overflow,
                player.getEnderPearls(),
                null
        );
    }

    /**
     * Apply a saved state to a player
     * @param player The player to apply state to
     * @param state The state to apply
     */
    public static void applyPlayerState(Player player, PlayerState state) {
        if (player == null || state == null) return;

        // Inventory & offhand
        player.getInventory().clear();
        player.getInventory().setContents(state.getInventory());
        player.getInventory().setArmorContents(state.getArmor());
        player.getInventory().setItemInOffHand(state.getOffhand());


        // Location
        if (state.getLocation() != null) {
            try { player.teleport(state.getLocation()); } catch (Throwable ignored) {}
        }

        // Vital stats (clamp)
        double max = BukkitCompat.getMaxHealthValue(player);
        double clamped = Math.max(0.0D, Math.min(max, state.getHealth()));
        try { player.setHealth(clamped); } catch (Throwable ignored) {}

        player.setFoodLevel(state.getFoodLevel());
        player.setSaturation(state.getSaturation());
        player.setExhaustion(state.getExhaustion());

        // Experience
        player.setTotalExperience(state.getTotalExperience());
        player.setExp(state.getExp());
        player.setLevel(state.getLevel());

        // Environment
        player.setFireTicks(state.getFireTicks());
        try { player.setMaximumAir(state.getMaximumAir()); } catch (Throwable ignored) {}
        try { player.setRemainingAir(state.getRemainingAir()); } catch (Throwable ignored) {}

        // Mode & motion
        if (state.getGameMode() != null) player.setGameMode(state.getGameMode());
        player.setFallDistance(state.getFallDistance());
        try { player.setAllowFlight(state.isAllowFlight()); } catch (Throwable ignored) {}
        try { player.setFlying(state.isFlying()); } catch (Throwable ignored) {}

        for (EnderPearl pearl : state.getAirbornePearls()) {
            pearl.setShooter(player);
        }

        // Effects
        try {
            for (org.bukkit.potion.PotionEffect e : player.getActivePotionEffects()) player.removePotionEffect(e.getType());
            if (state.getActivePotionEffects() != null) {
                for (org.bukkit.potion.PotionEffect e : state.getActivePotionEffects()) player.addPotionEffect(e);
            }
        } catch (Throwable ignored) {}

        // Misc
        try { player.setAbsorptionAmount(state.getAbsorptionAmount()); } catch (Throwable ignored) {}
        try { if (state.isGliding()) player.setGliding(true); } catch (Throwable ignored) {}
        try { player.setTicksLived(state.getTicksLived()); } catch (Throwable ignored) {}
        try { player.setNoDamageTicks(state.getNoDamageTicks()); } catch (Throwable ignored) {}
        try { player.setWalkSpeed(state.getWalkSpeed()); } catch (Throwable ignored) {}
        try { player.setFlySpeed(state.getFlySpeed()); } catch (Throwable ignored) {}
        try { player.setPortalCooldown(state.getPortalCooldown()); } catch (Throwable ignored) {}
        try {
            Entity vehicle = state.getVehicle();
            if (vehicle != null) {
                vehicle.addPassenger(player);
            }
        } catch (Throwable ignored) {}


        // manhunt
        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
            Map<Integer, ? extends ItemStack> stacks = player.getInventory().all(Material.COMPASS);
            for (Map.Entry<Integer, ? extends ItemStack> entry : stacks.entrySet()) {
                ItemStack compass = player.getInventory().getItem(entry.getKey());
                CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();

                compassMeta.setLodestoneTracked(false);
                if (state.getTrackingTarget() == null) {
                    System.err.println("TRACKIGN TARGET WAS NULL! ... thats actually fine if the previous player didn't click their compass");
                } else {
                    compassMeta.setLodestone(state.getTrackingTarget());
                    compass.setItemMeta(compassMeta);

                }
            }
        } else {
            Map<Integer, ? extends ItemStack> stacks = player.getInventory().all(Material.COMPASS);
            for (Map.Entry<Integer, ? extends ItemStack> entry : stacks.entrySet()) {
                ItemStack compass = player.getInventory().getItem(entry.getKey());
                CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();

                if (compassMeta.hasLodestone()) {
                    compass.setItemMeta((new ItemStack(Material.COMPASS)).getItemMeta());
                }
                if (state.getTrackingTarget() == null) {
                    System.err.println("TRACKIGN TARGET WAS NULL! ... thats actually fine if the previous player didn't click their compass");
                } else {
                    player.setCompassTarget(state.getTrackingTarget());
                }
            }
        }
    }

    public static void clear(Player player) {
        try {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[]{});
            player.getInventory().setItemInOffHand(null);
            player.getOpenInventory().setCursor(null);
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top instanceof CraftingInventory) {
                CraftingInventory craftingInv = (CraftingInventory) top;
                ItemStack[] empty = {};
                craftingInv.setMatrix(empty);
                craftingInv.setResult(ItemStack.empty());
            }

            player.updateInventory();
        } catch (Exception ignored) {
        }
    }

}
