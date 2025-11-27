package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.util.ActionBarUtil;
import com.shard.generalswap.util.BukkitCompat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Visualizer {

    public Visualizer() {

    }

    public void startActionBarUpdates() {
        Bukkit.getScheduler().runTaskTimer(SwapPlugin.get(), () -> {
            if (!SwapPlugin.get().getGameManager().gameStarted()) {
                return;
            }
            updateActionBar();
        }, 3L, 20);
    }

    private void updateActionBar() {
        if (!SwapPlugin.get().getGameManager().gameStarted()) return;

        for (Map.Entry<UUID, Body> player : SwapPlugin.get().getGameManager().playerInBody.get()) {
            if (player.getValue() != null) {
                long timeLeft = player.getValue().ticksTillNextSwap();
                String msg;
                if (timeLeft == -1) {
                    msg = String.format("§eNo Swap");
                } else {
                    msg = String.format("§eSwap in: §c%ds", Math.max(0, timeLeft) / 20);
                }
                Player msgMe = Bukkit.getPlayer(player.getKey());
                ActionBarUtil.sendActionBar(msgMe, msg);
                BukkitCompat.showTitle(msgMe, "", "", 0, Integer.MAX_VALUE, 0);
            } else {
                Player msgMe = Bukkit.getPlayer(player.getKey());
                ActionBarUtil.sendActionBar(msgMe, "");

                String t = "§6§lYou Are Swapped Out!";
                BukkitCompat.showTitle(msgMe, t, "", 0, Integer.MAX_VALUE, 0);


            }
        }
    }

}
