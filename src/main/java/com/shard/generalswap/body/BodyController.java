package com.shard.generalswap.body;

import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.state.PlayerRegistry;
import com.shard.generalswap.state.PlayerState;

import java.util.List;

// owns body
public final class BodyController {
    private final Body body;
    private final List<SwapStage> cycle;
    private final PlayerRegistry playerRegistry;
    private int index = 0;
    private boolean firstSwap = true;
    private final SwapOrchestrator orchestrator;

    public BodyController(int i, String name, List<SwapStage> cycle, SwapOrchestrator orchestrator, PlayerRegistry pr) {
        this.body = new Body(i, name);
        this.cycle = cycle;
        this.orchestrator = orchestrator;
        this.playerRegistry = pr;
    }

    public void start() {
        scheduleNext();
    }

    public String currentHost() {
        return cycle.get(index).playerName();
    }


    private void scheduleNext() {
        SwapStage stage = cycle.get(index);
        String playerOut;
        long durationTicks;
        if (firstSwap) {
            playerOut = null;
            durationTicks = 1;
        } else {
            playerOut = cycle.get((index - 1 + cycle.size()) % cycle.size()).playerName();
            durationTicks = stage.durationTicks();
        }
        firstSwap = false;

        orchestrator.scheduleSwap(
                stage.playerName(),
                playerOut,
                body,
                this,
                durationTicks
        );

    }

    /** Called by the orchestrator AFTER the swap is executed. */
    public void onSwapExecuted() {
        index = (index + 1) % cycle.size();
        scheduleNext();
    }
}
