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
        scheduleNext();
    }

    public Player currentHost() {
        return cycle.get(index).player();
    }


    private void scheduleNext() {
        SwapStage stage = cycle.get(index);
        Player playerOut;
        long durationTicks;
        if (firstSwap) {
            playerOut = null;
            durationTicks = 1;
        } else {
            playerOut = cycle.get((index - 1 + cycle.size()) % cycle.size()).player();
            durationTicks = stage.durationTicks();
        }
        firstSwap = false;

        body.setNextSwapTick(orchestrator.scheduleSwap(
                stage.player(),
                playerOut,
                body,
                this,
                durationTicks
        ));

    }



    /** Called by the orchestrator AFTER the swap is executed. */
    public void onSwapExecuted() {
        index = (index + 1) % cycle.size();
        scheduleNext();
    }
}
