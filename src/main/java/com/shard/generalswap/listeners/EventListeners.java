package com.shard.generalswap.listeners;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.state.PlayerState;
import com.shard.generalswap.util.PlayerStateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

}
