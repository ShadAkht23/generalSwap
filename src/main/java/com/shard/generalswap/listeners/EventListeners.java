package com.shard.generalswap.listeners;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.game.GameManager;
import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.game.Visualizer;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.state.PlayerState;
import com.shard.generalswap.util.Colours;
import com.shard.generalswap.util.PlayerStateUtil;
import io.papermc.paper.event.player.AbstractChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class EventListeners implements Listener {

    private final GameManager gameManager;
    private final PlayerInBody playerInBody;
    private final SwapOrchestrator swapOrchestrator;

    public EventListeners(GameManager gameManager, PlayerInBody playerInBody, SwapOrchestrator orchestrator) {
        this.gameManager = gameManager;
        this.playerInBody = playerInBody;
        this.swapOrchestrator = orchestrator;
    }

    // don't allow caged players to take damage
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        // Cancel any damage to inactive runners in cages
        if (!gameManager.gameStarted()) return;
        if (gameManager.isSwappedOut(victim.getUniqueId())) {
            //event.setCancelled(true);
        }

    }

    // handle spawn point tied to body not player
    @EventHandler
    public void onPlayerSpawnPointChange(PlayerSetSpawnEvent event) {
        if (!gameManager.gameStarted())
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
            Body body = playerInBody.getBody(p.getUniqueId());
            body.setSpawn(loc);
            System.out.println("player reset spawn location");
        }

    }

    // don't allow movement if in cage
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // If the player is an inactive runner, prevent movement
        if (gameManager.gameStarted() &&
                gameManager.isPlaying(player.getUniqueId()) &&
                gameManager.isSwappedOut(player.getUniqueId())) {

            // Only cancel if the player is actually trying to move (not just looking around)
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {

                event.setCancelled(true);
            }
        }
    }

    // save state when disconnect
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        if (!gameManager.gameStarted())
            return;
        PlayerState playerState = PlayerStateUtil.capturePlayerState(event.getPlayer());
        Body body = playerInBody.getBody(event.getPlayer().getUniqueId());
        if (body != null && !body.getName().equals("SWAPPED OUT")) {
            body.set(playerState);
        }
    }


    // if joined after swap and they are swapped in,
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!gameManager.gameStarted())
            return;

        Bukkit.getScheduler().runTaskLater(SwapPlugin.get(), () -> {
            UUID pid = event.getPlayer().getUniqueId();
            if (!playerInBody.isStateApplied(pid)) {

                Body body = playerInBody.getBody(pid);
                Player player = event.getPlayer();
                if (body == null) {
                    return;
                }
                if (body.getName().equals("SWAPPED OUT")) {
                    // swap them out
                    swapOrchestrator.doSwapOut(player);
                } else {
                    // swap them in
                    swapOrchestrator.doSwapIn(player, body);
                }
            }
        }, 10
        );
    }


    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (!gameManager.gameStarted()) {
            return;
        }
        Component msg = event.message();
        Component empty = Component.empty();
        //Component notEmpty = Component.empty().content("received advancement");
        event.message(null);
        if (msg == null)
            return;
        if (gameManager.gameStarted()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (gameManager.isSwappedOut(player.getUniqueId())) {
                    // only add if they are about to get swapped into the player who owns this body??
                    playerInBody.appendPendingChatMsg(player.getUniqueId(), msg);
                } else {
                    if (msg.toString().length() < 5)
                        return;
                    player.sendMessage(msg);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Component msg = event.deathMessage();
        if (msg == null)
            return;
        if (!gameManager.gameStarted()) {
            return;
        }

        // MESSAGE MANAGEMENT
        event.setShowDeathMessages(false);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameManager.isSwappedOut(player.getUniqueId())) {
                playerInBody.appendPendingChatMsg(player.getUniqueId(), msg);
            } else {
                player.sendMessage(msg);
            }
        }

    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (!gameManager.gameStarted())
            return;
        if (!gameManager.isRunnerRn(event.getPlayer().getUniqueId()))
            return;
        Body body = playerInBody.getBody(event.getPlayer().getUniqueId());
        Location from = event.getFrom();
        if (from.getWorld().getEnvironment() == World.Environment.NORMAL) {
            body.setLastKnownOverworldLoc(from);
        } else if (from.getWorld().getEnvironment() == World.Environment.NETHER) {
            body.setLastKnownNetherLoc(from);
        }
    }

}