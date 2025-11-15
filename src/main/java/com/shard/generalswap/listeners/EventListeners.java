package com.shard.generalswap.listeners;

import com.shard.generalswap.SwapPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.EventExecutor;

public class EventListeners implements Listener {

    private SwapPlugin plugin = SwapPlugin.get();


    // don't allow caged players to take damage
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        // Cancel any damage to inactive runners in cages
        if (!plugin.getGameManager().gameStarted()) return;
        if (plugin.getGameManager().isSwappedOut(victim)) {
            event.setCancelled(true);
        }

    }


    // change spawn for body when host changes spawn.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawnChange(PlayerRespawnEvent event) {
        if (!event.isBedSpawn() && !event.isAnchorSpawn()) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null || plugin.getGameManager().playerInBody.isSwappedOut(player)) {
            return;
        }
        org.bukkit.Location spawn = null;
        try {
            java.lang.reflect.Method locator = player.getClass().getMethod("getRespawnLocation");
            Object result = locator.invoke(player);
            if (result instanceof org.bukkit.Location loc) {
                spawn = loc;
            }
        } catch (NoSuchMethodException ignored) {
            // Older API, fall back to event location
        } catch (Throwable reflectiveFailure) {
            plugin.getLogger().fine("Failed to read respawn location reflectively: " + reflectiveFailure.getMessage());
        }
        if (spawn == null) {
            spawn = event.getRespawnLocation();
        }
        if (spawn != null && spawn.getWorld() != null) {

            plugin.getGameManager().playerInBody.getBody(player).setSpawn(spawn);
        }
    }


    // don't allow movement if in cage
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // If the player is an inactive runner, prevent movement
        if (plugin.getGameManager().gameStarted() &&
                plugin.getGameManager().isSwappedOut(player)) {

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
