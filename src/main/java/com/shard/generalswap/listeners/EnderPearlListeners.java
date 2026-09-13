package com.shard.generalswap.listeners;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.game.GameManager;
import com.shard.generalswap.state.PlayerInBody;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class EnderPearlListeners implements Listener {
    private final SwapPlugin plugin = SwapPlugin.get();
    private final GameManager gameManager;
    private final PlayerInBody playerInBody;

    public EnderPearlListeners(GameManager gameManager, PlayerInBody playerInBody) {
        this.gameManager = gameManager;
        this.playerInBody = playerInBody;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Player player = event.getPlayer();
            if (player.hasMetadata("cancel_next_pearl")) {
                event.setCancelled(true);
                player.removeMetadata("cancel_next_pearl", plugin);
            }

        }
    }

    @EventHandler
    public void OnProjectileHit(ProjectileHitEvent event) {
        if (!gameManager.gameStarted())
            return;
        if (event.getEntity() instanceof EnderPearl) {
            EnderPearl pearl = (EnderPearl) event.getEntity();
            UUID uuid = playerInBody.getPendingEnderPearlSwap(pearl);
            if (uuid != null) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    // they joined back. set them to be shooter and let the event do its thing
                    pearl.setShooter(player);
                    playerInBody.pearlNotPending(pearl);
                } else {
                    // still offline.
                    // get location of pearl landing:
                    Location loc = null;
                    Block block = event.getHitBlock();
                    if (block != null) {
                        loc = block.getLocation();
                    }

                    Entity entity = event.getHitEntity();
                    if (entity != null) {
                        loc = entity.getLocation();
                    }
                    if (loc == null) {
                        System.err.println("Couldn't resolve pearl landing position");
                    }
                    Body body = playerInBody.getBody(uuid);
                    body.setPearlLand(loc);
                    ((Player)pearl.getShooter()).setMetadata("cancel_next_pearl", new FixedMetadataValue(plugin, true));
                    System.out.println("cancelling projectilie event");
                }
            }
        }
    }
}
