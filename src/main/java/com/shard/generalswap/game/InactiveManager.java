package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.util.BukkitCompat;
import com.shard.generalswap.util.PlayerStateUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InactiveManager {

    private final Map<World, List<BlockState>> sharedCageBlocks;
    private final Map<World, Location> sharedCageCenters;
    private final Set<UUID> cagedPlayers;

    public InactiveManager() {
        cagedPlayers = new HashSet<>();
        sharedCageCenters = new java.util.HashMap<>();
        sharedCageBlocks  = new java.util.HashMap<>();
    }
    public void makeInactive(@NotNull Player player) {
        applyInactiveEffects(player);
        createOrEnsureSharedCage(player.getWorld());
        teleportToSharedCage(player);
    }

    public Set<UUID> getCagedPlayers() {
        return cagedPlayers;
    }

    public void makeActive(@NotNull Player player) {
        cagedPlayers.remove(player.getUniqueId());

        player.setGameMode(GameMode.SURVIVAL);
        makeVisible(player);
    }

    public void makeVisible(@NotNull Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(player)) {
                viewer.showPlayer(SwapPlugin.get(), player);
            }
        }
    }

    public void makeInvisible(@NotNull Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(player)) {
                viewer.hidePlayer(SwapPlugin.get(), player);
            }
        }
    }

    private void teleportToSharedCage(Player p) {
        if (p == null || !p.isOnline()) return;
        createOrEnsureSharedCage(p.getWorld());
        org.bukkit.Location center = sharedCageCenters.get(p.getWorld());
        if (center != null) {
            // Teleport player to the center of the cage floor

            p.teleport(center);
            //p.teleport(new Location(p.getWorld(), 0, -70, 0));
            cagedPlayers.add(p.getUniqueId());
            try { p.setAllowFlight(true); } catch (Exception ignored) {}
            try { p.setFlying(false); } catch (Exception ignored) {}
        }
    }

    private void createOrEnsureSharedCage(World world) {
        if (world == null) world = Bukkit.getWorlds().get(0);
        int y = world.getMaxHeight() - 10;

        int cx = 0;
        int cz = 0;
        Location center = new Location(world, cx + 0.5, y, cz + 0.5);
        Location existing = sharedCageCenters.get(world);
        if (existing != null && Math.abs(existing.getX() - center.getX()) < 0.1 && Math.abs(existing.getY() - center.getY()) < 0.1 && Math.abs(existing.getZ() - center.getZ()) < 0.1) {
            return;
        }
        // Cleanup old cage in this world
        java.util.List<org.bukkit.block.BlockState> old = sharedCageBlocks.remove(world);
        if (old != null) {
            for (BlockState s : old) { try { s.update(true, false); } catch (Exception ignored) {} }
        }
        try { center.getChunk().load(true); } catch (Throwable ignored) {}
        List<BlockState> changed = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    Block block = world.getBlockAt(cx + dx, y + dy, cz + dz);
                    boolean isShell = (dx == -2 || dx == 2 || dz == -2 || dz == 2 || dy == -1 || dy == 2);
                    changed.add(block.getState());
                    block.setType(isShell ? Material.BEDROCK : Material.AIR, false);
                }
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                org.bukkit.block.Block floor = world.getBlockAt(cx + dx, y - 1, cz + dz);
                changed.add(floor.getState());
                floor.setType(Material.BEDROCK, false);
            }
        }
        sharedCageBlocks.put(world, changed);
        sharedCageCenters.put(world, center.clone());
    }

    public void cleanupAllCages() {
        for (Map.Entry<org.bukkit.World, List<BlockState>> e : sharedCageBlocks.entrySet()) {
            List<BlockState> list = e.getValue();
            if (list != null) for (BlockState s : list) { try { s.update(true, false); } catch (Exception ignored) {} }
        }
        sharedCageBlocks.clear();
        sharedCageCenters.clear();
        cagedPlayers.clear();
    }

    private void applyInactiveEffects(Player player) {
        PlayerStateUtil.clear(player);
        player.getInventory().setArmorContents(new ItemStack[]{});
        player.getInventory().setItemInOffHand(null);
        player.updateInventory();

        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue());
        player.setFoodLevel(20);



        PotionEffectType blindness = BukkitCompat.resolvePotionEffect("blindness");
        if (blindness != null)
            player.addPotionEffect(new PotionEffect(blindness, Integer.MAX_VALUE, 1, false, false));
        PotionEffectType invis = BukkitCompat.resolvePotionEffect("invisibility");
        if (invis != null)
            player.addPotionEffect(new PotionEffect(invis, Integer.MAX_VALUE, 1, false, false));
        player.setGameMode(GameMode.ADVENTURE);
        // Allow flight while caged to prevent server kicking for "flying"
        try {
            player.setAllowFlight(true);
        } catch (Exception ignored) {
        }
        try {
            player.setFlying(false);
        } catch (Exception ignored) {
        }

        makeInvisible(player);

    }
}
