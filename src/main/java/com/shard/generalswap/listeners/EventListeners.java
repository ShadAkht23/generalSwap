package com.shard.generalswap.listeners;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.state.PlayerState;
import com.shard.generalswap.util.PlayerStateUtil;
import io.papermc.paper.event.player.AbstractChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class EventListeners implements Listener {

    private SwapPlugin plugin = SwapPlugin.get();


    // don't allow caged players to take damage
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        // Cancel any damage to inactive runners in cages
        if (!plugin.getGameManager().gameStarted()) return;
        if (plugin.getGameManager().isSwappedOut(victim.getUniqueId())) {
            //event.setCancelled(true);
        }

    }


    // handle spawn point tied to body not player
    @EventHandler
    public void onPlayerSpawnPointChange(PlayerSetSpawnEvent event) {
        if (!plugin.getGameManager().gameStarted())
            return;
        if (!(event.getCause() == PlayerSetSpawnEvent.Cause.BED ||
            event.getCause() == PlayerSetSpawnEvent.Cause.RESPAWN_ANCHOR ||
            event.getCause() == PlayerSetSpawnEvent.Cause.PLAYER_RESPAWN ||
            event.getCause() == PlayerSetSpawnEvent.Cause.COMMAND)) {
            return;
        }
        Player p  = event.getPlayer();
        Location loc = event.getLocation();
        if (loc != null) {
            plugin.getGameManager().playerInBody.getBody(p.getUniqueId()).setSpawn(loc);
            System.out.println("player reset bed");
        }

    }


    // don't allow movement if in cage
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // If the player is an inactive runner, prevent movement
        if (plugin.getGameManager().gameStarted() &&
                plugin.getGameManager().isSwappedOut(player.getUniqueId())) {

            // Check if getTo() is not null to prevent NullPointerException
            if (event.getTo() != null) {
                // Only cancel if the player is actually trying to move (not just looking around)
                if (event.getFrom().getX() != event.getTo().getX() ||
                        event.getFrom().getY() != event.getTo().getY() ||
                        event.getFrom().getZ() != event.getTo().getZ()) {

                    event.setCancelled(true);
                }
            }
        }
    }



    // save state when disconnect
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        if (!SwapPlugin.get().getGameManager().gameStarted())
            return;
        PlayerInBody playerInBody = plugin.getGameManager().playerInBody;
        PlayerState playerState = PlayerStateUtil.capturePlayerState(event.getPlayer());
        Body body = playerInBody.getBody(event.getPlayer().getUniqueId());
        if (body != null) {
            body.set(playerState);
        }
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
        if (!SwapPlugin.get().getGameManager().gameStarted())
            return;
        PlayerInBody playerInBody = SwapPlugin.get().getGameManager().playerInBody;
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
                    playerInBody.getBody(uuid).setPearlLand(loc);
                    ((Player)pearl.getShooter()).setMetadata("cancel_next_pearl", new FixedMetadataValue(plugin, true));
                    System.out.println("cancelling projectilie event");
                }
            }
        }
    }



    // if joined after swap and they are swapped in,
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!SwapPlugin.get().getGameManager().gameStarted())
            return;

        Bukkit.getScheduler().runTaskLater(SwapPlugin.get(), () -> {
            UUID pid = event.getPlayer().getUniqueId();
            PlayerInBody playerInBody = SwapPlugin.get().getGameManager().playerInBody;
            if (!playerInBody.isStateApplied(pid)) {
                System.out.println("Later: doing something");

                Body body = playerInBody.getBody(pid);
                Player player = event.getPlayer();
                SwapOrchestrator orchestrator = SwapPlugin.get().getGameManager().getOrchestrator();

                if (body == null) {
                    // swap them out
                    System.out.println("Later: Swapping out");
                    orchestrator.doSwapOut(player);
                } else {
                    // swap them in
                    System.out.println("Later: Swapping in");

                    orchestrator.doSwapIn(player, body);
                }
            }
        }, 10
        );
    }


    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Component msg = event.message();
        Component empty = Component.empty();
        //Component notEmpty = Component.empty().content("received advancement");
        event.message(empty);
        if (msg == null)
            return;
        if (plugin.getGameManager().gameStarted()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getGameManager().isSwappedOut(player.getUniqueId())) {
                    // only add if they are about to get swapped into the player who owns this body??
                    plugin.getGameManager().playerInBody.appendPendingChatMsg(player.getUniqueId(), msg);
                } else {

                    player.sendMessage(msg);
                }
            }
        }
    }

}
