package com.shard.generalswap.game;

import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.SwapEvent;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.util.PlayerStateUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SwapOrchestrator {

    private long currentTick = 0;

    private final Map<Long, List<SwapEvent>> queue = new HashMap<>();

    private final InactiveManager inactiveManager;
    private final PlayerInBody playerInBody;

    public SwapOrchestrator(InactiveManager inactiveManager, PlayerInBody playerInBody) {
        this.inactiveManager = inactiveManager;
        this.playerInBody = playerInBody;
    }

    public void reset() {
        currentTick = 0;
        queue.clear();
    }

    public long timeSinceBegan() {
        return currentTick;
    }

    public void tick() {
        currentTick++;

        List<SwapEvent> events = queue.remove(currentTick);
        if (events == null) return;
        //System.out.println("executing swaps at tick " + currentTick + ":");
        executeSwaps(events);
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public long scheduleSwap(UUID playerIn, UUID playerOut, Body state, BodyController controller, long delayTicks) {
        long target = currentTick + delayTicks;
        queue.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new SwapEvent(playerIn, playerOut, state, controller));
        return target;
    }

    public void doSwapIn(@NotNull Player player, Body body) {
        body.applyPlayerState(player);
        inactiveManager.makeActive(player);
        //player.sendMessage("you have been swapped into body: " + body.getName());
        playerInBody.appliedState(player.getUniqueId());

    }
    public void doSwapOut(@NotNull Player player) {
        inactiveManager.makeInactive(player);
        // player.sendMessage("you are swapped out rn");
        playerInBody.appliedState(player.getUniqueId());
    }

    private void executeSwaps(List<SwapEvent> events) {
        // perform the actual player→body swap logic

        // event.playerOut() may be null for first swap.
        for (SwapEvent event : events) {
            if (playerInBody.isSwappedOut(event.playerIn())) {
                VoiceChannelSwapper.swapIn(event.playerIn());
            }
        }

        Set<UUID> playersInVoid = new HashSet<>();
        for (SwapEvent event : events) {
            System.out.println("Player " + event.playerIn() + "  being swapped into " + event.state().getName() +  " displacing " + event.playerOut());
            // a player is being swapped out of the body

            // save that player's state into the body state
            if (event.playerOut() != null) {
                playerInBody.swapOut(event.playerOut());
                playersInVoid.add(event.playerOut());
                Player playOut = Bukkit.getPlayer(event.playerOut());
                if (playOut != null) {
                    event.state().set(PlayerStateUtil.capturePlayerState(playOut));
                    inactiveManager.makeInvisible(playOut);
                }
            }
        }

        for (SwapEvent event : events) {
            // set player in's state to be the body its going to inhabit
            // if player offline, do this stuff in the join event.
            playersInVoid.remove(event.playerIn());

            playerInBody.switchBody(event.playerIn(), event.state());

            Player playIn = Bukkit.getPlayer(event.playerIn());
            if (playIn != null) {
                doSwapIn(playIn, event.state());
            } else {
                // if playOut has any airborne enderpearls, we need to track them now!
                // pearl Tracker: Pearl -> new owner?
                Player playOut = Bukkit.getPlayer(event.playerOut());
                if (playOut != null) {
                    for (EnderPearl enderPearl : playOut.getEnderPearls()) {
                        //enderPearl.setShooter(null);
                        playerInBody.addPendingPearlSwap(enderPearl, event.playerIn());
                        System.out.println("adding pending enderpearl swap");
                    }
                }
                playerInBody.applyStateLater(event.playerIn());
            }
        }

        // we make swapping out players invisible then visible in the same tick to force a refresh.
        // fixes desync issues when using boats
        for (SwapEvent event: events) {
            if (event.playerOut() != null) {
                Player playOut = Bukkit.getPlayer(event.playerOut());
                if (playOut != null) {
                    inactiveManager.makeVisible(playOut);
                }
            }
        }

        // players in void contains players who are swapped out but not swapped in.
        for (UUID pid : playersInVoid) {
            Player player = Bukkit.getPlayer(pid);
            playerInBody.swapOut(pid);
            VoiceChannelSwapper.swapOut(pid);
            if (player != null) {
                doSwapOut(player);
            } else {
                // make them inactive later.
                playerInBody.applyStateLater(pid);
            }
        }

        // UPDATING SWAPPED OUT TIMER
        List<Map<UUID, Long>> tickMaps = new ArrayList<>();
        for (SwapEvent event : events) {
            Map<UUID, Long> tickMap = event.callBack().nextSwapTicks();
            tickMaps.add(tickMap);
        }

        Map<UUID, Long> union = new HashMap<>();
        for (Map<UUID, Long> tickMap : tickMaps) {
            for (Map.Entry<UUID, Long> entry : tickMap.entrySet()) {
                if (union.containsKey(entry.getKey())) {
                    union.put(entry.getKey(), Math.min(union.get(entry.getKey()), entry.getValue()));
                } else {
                    union.put(entry.getKey(), entry.getValue());
                }
            }
        }
        for (Map.Entry<UUID, Long> entry : union.entrySet()) {
            playerInBody.updateNextSwapIn(entry.getKey(), entry.getValue());
        }
        // callback: for scheduling next swap
        for (SwapEvent event : events) {
            event.callBack().onSwapExecuted();
        }
    }


}
