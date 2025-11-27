package com.shard.generalswap.listeners;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import com.shard.generalswap.SwapPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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



}
