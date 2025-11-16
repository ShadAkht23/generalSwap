package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.util.ActionBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public class Visualizer {

    public Visualizer() {

    }


    public void startActionBarUpdates() {
        Bukkit.getScheduler().runTaskTimer(SwapPlugin.get(), () -> {
            if (!SwapPlugin.get().getGameManager().gameStarted()) {
                return;
            }
            updateActionBar();
        }, 2L, 20);
    }

    private void updateActionBar() {
        if (!SwapPlugin.get().getGameManager().gameStarted()) return;

        for (Map.Entry<Player, Body> player : SwapPlugin.get().getGameManager().playerInBody.get()) {
            if (player.getValue() != null) {
                long timeLeft = player.getValue().ticksTillNextSwap();
                String msg = String.format("§eSwap in: §c%ds", Math.max(0, timeLeft) / 20);
                ActionBarUtil.sendActionBar(player.getKey(), msg);
            } else {

                ActionBarUtil.sendActionBar(player.getKey(), "");
            }
        }
    }

}
