package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.state.PlayerInBody;
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

    private final Set<UUID> cagedPlayers;

    public InactiveManager() {
        cagedPlayers = new HashSet<>();
    }
    public void makeInactive(@NotNull Player player) {
        applyInactiveEffects(player);
        //createOrEnsureSharedCage(player.getWorld());


        try { player.setAllowFlight(true); } catch (Exception ignored) {}
        try { player.setFlying(false); } catch (Exception ignored) {}

        player.teleport(new Location(player.getWorld(), 0, 50000, 0));
        //teleportToSharedCage(player);
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
