package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.SwapEvent;
import com.shard.generalswap.util.PlayerStateUtil;
import com.shard.generalswap.util.Scheduler;
import org.bukkit.entity.Player;

import java.util.*;

public class SwapOrchestrator {

    //private final BodyRegistry bodies;
    private final Scheduler scheduler;

    private long currentTick = 0;

    private final Map<Long, List<SwapEvent>> queue = new HashMap<>();

    private InactiveManager inactiveManager;

    public SwapOrchestrator(InactiveManager inactiveManager) {
       // this.bodies = bodies;
        this.scheduler = new Scheduler(SwapPlugin.get());
        this.inactiveManager = inactiveManager;
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

        GameManager gameManager = SwapPlugin.get().getGameManager();

        // event.playerOut() may be null for first swap.
        Set<Player> playersInVoid = new HashSet<>();
        for (SwapEvent event : events) {
            // a player is being swapped out of the body
            // save that player's state into the body state
            gameManager.playerInBody.switchBody(event.playerOut(), null);

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
            inactiveManager.makeActive(event.playerIn());
            gameManager.playerInBody.switchBody(event.playerIn(), event.state());

            event.playerIn().sendMessage("you have been swapped in!");
        }
        // players in void contains players who are swapped out but not swapped in.

        for (Player player : playersInVoid) {
            inactiveManager.makeInactive(player);

            player.sendMessage("you are swapped out rn");
        }
        //playerRegistry.print();

        // callback:
        for (SwapEvent event : events) {
            event.callBack().onSwapExecuted();
        }
    }


}
