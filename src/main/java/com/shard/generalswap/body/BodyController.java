package com.shard.generalswap.body;

import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.state.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

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
        orchestrator.scheduleSwap(cycle.get(0).pid(), null, body, this, 1);
    }

    public UUID currentHost() {
        return cycle.get(index).pid();
    }


    private void scheduleNext() {
        SwapStage stage = cycle.get(index);
        UUID playerOut;
        long durationTicks = 0;
        playerOut = stage.pid();
        UUID playerIn;
        int i = 0;
        SwapStage oldStage = stage;
        do {
            i++;
            SwapStage newstage = cycle.get((index + i) % cycle.size());
            playerIn = newstage.pid();
            durationTicks += oldStage.durationTicks();
            oldStage = newstage;
        } while (playerIn == playerOut && i < cycle.size());
        if (playerIn != playerOut) {
            firstSwap = false;
            index = (index + i) % cycle.size();

            body.setNextSwapTick(orchestrator.scheduleSwap(
                    playerIn,
                    playerOut,
                    body,
                    this,
                    durationTicks
            ));
        } else {
            body.setNextSwapTick(-1);
        }
    }



    /** Called by the orchestrator AFTER the swap is executed. */
    public void onSwapExecuted() {
        scheduleNext();
    }
}
