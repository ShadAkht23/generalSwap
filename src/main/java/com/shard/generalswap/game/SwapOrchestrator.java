package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.SwapEvent;
import com.shard.generalswap.body.BodyRegistry;
import com.shard.generalswap.state.PlayerRegistry;
import com.shard.generalswap.util.BukkitCompat;
import com.shard.generalswap.util.PlayerStateUtil;
import com.shard.generalswap.util.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SwapOrchestrator {

    //private final BodyRegistry bodies;
    private final PlayerRegistry playerRegistry;
    private final Scheduler scheduler;
    private long currentTick = 0;

    private final Map<Long, List<SwapEvent>> queue = new HashMap<>();

    public SwapOrchestrator(
                            PlayerRegistry states) {
       // this.bodies = bodies;
        this.playerRegistry = states;
        this.scheduler = new Scheduler(SwapPlugin.get());
    }

    public void tick() {
        currentTick++;

        List<SwapEvent> events = queue.remove(currentTick);
        if (events == null) return;
        System.out.println("executing swaps at tick " + currentTick + ":");
        executeSwaps(events);
        /*for (SwapEvent event : events) {
            executeSwap(event);
            event.controller().onSwapExecuted();
        }*/
    }

    public void scheduleSwap(Player playerIn, Player playerOut, Body state, BodyController controller, long delayTicks) {
        long target = currentTick + delayTicks;
        queue.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new SwapEvent(playerIn, playerOut, state, controller));
    }

    private void executeSwaps(List<SwapEvent> events) {
        // TODO: perform the actual player→body swap logic

        // event.playerOut() may be null for first swap.
        Set<Player> playersInVoid = new HashSet<>();
        for (SwapEvent event : events) {
            // a player is being swapped out of the body
            // save that player's state into the body state
            if (event.playerOut() != null) {
                playersInVoid.add(event.playerOut());
                event.state().set(PlayerStateUtil.capturePlayerState(event.playerOut()));
            } else {
                //event.state().set(0); // the empty state (ie empty inventory etc )
            }
        }

        for (SwapEvent event : events) {
            // set player in's state to be the body its going to inhabit
            playersInVoid.remove(event.playerIn());
            event.state().applyPlayerState(event.playerIn());
            event.playerIn().sendMessage("you have been swapped in!");
        }
        // players in void contains players who are swapped out but not swapped in.

        for (Player player : playersInVoid) {
            applyInactiveEffects(player);

            player.sendMessage("you are swapped out rn");
        }
        //playerRegistry.print();

        // callback:
        for (SwapEvent event : events) {
            event.callBack().onSwapExecuted();
        }
    }

    public void applyInactiveEffects(Player player) {
        PlayerStateUtil.clear(player);
        player.getInventory().setArmorContents(new ItemStack[]{});
        player.getInventory().setItemInOffHand(null);
        player.updateInventory();

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

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(player)) {
                viewer.hidePlayer(SwapPlugin.get(), player);
            }
        }
    }
}
