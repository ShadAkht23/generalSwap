package com.shard.generalswap.body;

import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.state.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

// owns body
public final class BodyController {
    private final Body body;
    private final List<SwapStage> cycle;
    //private final PlayerRegistry playerRegistry;
    private int index = 0;
    private boolean firstSwap = true;
    private final SwapOrchestrator orchestrator;

    public BodyController(PlayerState ps, String name, List<SwapStage> cycle, SwapOrchestrator orchestrator) {
        this.body = new Body(ps, name);
        this.cycle = cycle;
        this.orchestrator = orchestrator;
        //this.playerRegistry = pr;
    }

    public void start() {
        orchestrator.scheduleSwap(cycle.get(0).player(), null, body, this, 1);
    }

    public Player currentHost() {
        return cycle.get(index).player();
    }


    private void scheduleNext() {
        SwapStage stage = cycle.get(index);
        Player playerOut;
        long durationTicks;
        playerOut = stage.player();
        Player playerIn = cycle.get((index + 1) % cycle.size()).player();
        durationTicks = stage.durationTicks();

        firstSwap = false;

        body.setNextSwapTick(orchestrator.scheduleSwap(
                playerIn,
                playerOut,
                body,
                this,
                durationTicks
        ));

    }



    /** Called by the orchestrator AFTER the swap is executed. */
    public void onSwapExecuted() {
        scheduleNext();
        index = (index + 1) % cycle.size();
    }
}
