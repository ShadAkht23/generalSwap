package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.SwapEvent;
import com.shard.generalswap.body.BodyRegistry;
import com.shard.generalswap.state.PlayerRegistry;
import com.shard.generalswap.util.Scheduler;

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

    public void scheduleSwap(String playerIn, String playerOut, Body state, BodyController controller, long delayTicks) {
        long target = currentTick + delayTicks;
        queue.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new SwapEvent(playerIn, playerOut, state, controller));
    }

    private void executeSwaps(List<SwapEvent> events) {
        // TODO: perform the actual player→body swap logic

        // event.playerOut() may be null for first swap.
        Set<String> playersInVoid = new HashSet<>();
        for (SwapEvent event : events) {
            // a player is being swapped out of the body
            // save that player's state into the body state
            if (event.playerOut() != null) {
                playersInVoid.add(event.playerOut());
                event.state().set(playerRegistry.get(event.playerOut()));
                playerRegistry.save(event.playerOut(), -1); // in void.
            } else {
                //event.state().set(0); // the empty state (ie empty inventory etc )
            }
        }

        for (SwapEvent event : events) {
            // set player in's state to be the body its going to inhabit
            playersInVoid.remove(event.playerIn());
            playerRegistry.save(event.playerIn(), event.state().get());
        }
        for (String player : playersInVoid) {
            playerRegistry.save(player, -1);
        }
        playerRegistry.print();

        // callback:
        for (SwapEvent event : events) {
            event.callBack().onSwapExecuted();
        }
    }
}
