package com.shard.generalswap.listeners;

import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.game.GameManager;
import com.shard.generalswap.game.Visualizer;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.util.Colours;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public class CompassListeners implements Listener {

    private final GameManager gameManager;
    private final PlayerInBody playerInBody;
    private final Visualizer visualizer;


    public CompassListeners(GameManager gameManager, PlayerInBody playerInBody, Visualizer visualizer) {
        this.gameManager = gameManager;
        this.playerInBody = playerInBody;
        this.visualizer = visualizer;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!gameManager.gameStarted()) {
            return;
        }
        // HUNTER DON't DROP COMPASS
        if (gameManager.isHunterRn(event.getPlayer().getUniqueId())) {
            event.getDrops().removeIf(stack -> stack.getType() == Material.COMPASS);
        }
    }

    @EventHandler
    public void onHunterDropCompass(PlayerDropItemEvent event) {
        if (!gameManager.gameStarted())
            return;
        if (gameManager.isHunterRn(event.getPlayer().getUniqueId())) {
            if (event.getItemDrop().getItemStack().getType() == Material.COMPASS) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!gameManager.gameStarted())
            return;

        if (gameManager.isHunterRn(event.getPlayer().getUniqueId())) {
            event.getPlayer().give(new ItemStack(Material.COMPASS));
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!gameManager.gameStarted())
            return;
        Player player = event.getPlayer();
        if (!gameManager.isHunterRn(player.getUniqueId()))
            return;

        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
            ItemStack compass = player.getInventory().getItemInMainHand();
            CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();

            if (event.getAction().isRightClick()) {
                // RESET COMPASS POSITION!

                // runner they're tracking
                Body body = playerInBody.getBody(player.getUniqueId());
                BodyController runnerBody = body.pointingTo();
                Player runner = Bukkit.getPlayer(runnerBody.currentHost());
                if (runner == null) {
                    player.sendMessage("Runner not in server. Could not track");
                    return;
                }
                Location runnerLoc = runner.getLocation();
                World.Environment playerEnv = player.getWorld().getEnvironment();
                World.Environment runnerEnv = runnerLoc.getWorld().getEnvironment();
                if (playerEnv != runnerEnv &&
                        playerEnv == World.Environment.NORMAL) {
                    // use last known overworld position.
                    Location lastKnown = runnerBody.getBody().getLastKnownOverworldLoc();
                    if (lastKnown == null) {
                        visualizer.trackingUpdateBadDimension(player, runner.getName());
                        return;
                    }
                    runnerLoc = lastKnown;
                    player.sendMessage(Component.text("Using" + runner.getName() + "'s last known location").color(Colours.DarkAqua));
                }
                if (playerEnv != runnerLoc.getWorld().getEnvironment() &&
                        playerEnv == World.Environment.NETHER) {

                    Location lastKnown = runnerBody.getBody().getLastKnownNetherLoc();
                    if (lastKnown == null) {
                        visualizer.trackingUpdateBadDimension(player, runner.getName());
                        return;
                    }
                    runnerLoc = lastKnown;
                    player.sendMessage(
                            Component.text("Using " + runner.getName() + "'s last known location").color(Colours.DarkAqua));
                }


                if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
                    setCompassPointNether(compass, compassMeta, runnerLoc);
                    body.setTrackingPos(runnerLoc);

                    visualizer.trackingUpdateSuccess(player, runner.getName());
                } else {
                    setCompassPointOverworld(player, compass, compassMeta, runnerLoc);

                    body.setTrackingPos(runnerLoc);
                    visualizer.trackingUpdateSuccess(player, runner.getName());
                    // TODO might have to use getCompassTarget for the swap.
                }

            }
            if (event.getAction().isLeftClick()) {
                // switch runner!
                Body body = playerInBody.getBody(player.getUniqueId());
                BodyController runnerBody = body.pointingTo();
                body.incRunnerIdx();
            }

        }
    }

    private void setCompassPointOverworld(Player hunter, ItemStack compass ,CompassMeta compassMeta, Location loc) {
        if (compassMeta.hasLodestone()) {
            compass.setItemMeta((new ItemStack(Material.COMPASS)).getItemMeta());
        }
        hunter.setCompassTarget(loc);
    }

    private void setCompassPointNether(ItemStack compass, CompassMeta compassMeta, Location loc) {
        compassMeta.setLodestoneTracked(false);
        compassMeta.setLodestone(loc);
        compass.setItemMeta(compassMeta);
    }
}
