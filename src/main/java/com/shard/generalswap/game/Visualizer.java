package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.util.ActionBarUtil;
import com.shard.generalswap.util.BukkitCompat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Visualizer {

    private BukkitTask actionBarTask;
    private Map<UUID, ActionBarTexts> barTexts;

    public Visualizer() {
        barTexts = new HashMap<>();
    }

    public void startActionBarUpdates() {
        actionBarTask = Bukkit.getScheduler().runTaskTimer(SwapPlugin.get(), () -> {
            if (!SwapPlugin.get().getGameManager().gameStarted()) {
                return;
            }
            updateActionBar();
        }, 3L, 20);
    }

    public void stopActionBarUpdates() {
        actionBarTask.cancel();
        for (UUID uuid: SwapPlugin.get().getGameManager().getInactiveManager().getCagedPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                BukkitCompat.showTitle(player, "", "", 0, Integer.MAX_VALUE, 0);
            }
        }
    }
    private void updateSwapIn(UUID uuid, String msg) {
        ActionBarTexts texts = barTexts.get(uuid);
        if (texts == null) {
            texts = new ActionBarTexts();
        }
        texts.nextSwapIn = msg;
        barTexts.put(uuid, texts);
    }

    private void updateTracking(UUID uuid, String msg) {
        ActionBarTexts texts = barTexts.get(uuid);
        if (texts == null) {
            texts = new ActionBarTexts();
        }
        texts.trackingMsg = msg;
        texts.trackingTick = Bukkit.getCurrentTick();
    }

    private void updateBarPlayer(UUID uuid) {
        ActionBarTexts texts = barTexts.get(uuid);
        if (texts == null)
            return;
        String msg = "";
        if (texts.nextSwapIn != null)
            msg = texts.nextSwapIn;
        if (texts.trackingMsg != null) {
            if (Bukkit.getCurrentTick() - texts.trackingTick < 40) {
                msg = msg + "  §b|  " + texts.trackingMsg;
            } else {
                texts.trackingMsg = null;
            }
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null)
            ActionBarUtil.sendActionBar(player, msg);
    }

    public  void trackingUpdateSuccess(Player hunter, String runnerName) {
        String msg = String.format("§aTracking " + runnerName);
        updateTracking(hunter.getUniqueId(), msg);
        updateBarPlayer(hunter.getUniqueId());
        //ActionBarUtil.sendActionBar(hunter, msg);
    }

    public  void trackingUpdateBadDimension(Player hunter, String runnerName) {
        String msg = String.format("§c" + runnerName + " is not your dimension");
        updateTracking(hunter.getUniqueId(), msg);
        updateBarPlayer(hunter.getUniqueId());

    }

    private void updateActionBar() {
        if (!SwapPlugin.get().getGameManager().gameStarted()) return;
        PlayerInBody playerInBody = SwapPlugin.get().getGameManager().playerInBody;
        for (Map.Entry<UUID, Body> player : playerInBody.get()) {
            if (player.getValue() == null)
                continue;
            if (!player.getValue().getName().equals("SWAPPED OUT")) {
                long timeLeft = player.getValue().ticksTillNextSwap();
                String msg;
                Player msgMe = Bukkit.getPlayer(player.getKey());
                if (timeLeft == -1) {
                    msg = "§eNo Swap";
                } else {
                    msg = String.format("§eSwap in: §c%ds", Math.max(0, timeLeft) / 20);
                    updateSwapIn(player.getKey(), msg);
                    updateBarPlayer(player.getKey());
                    //ActionBarUtil.sendActionBar(msgMe, msg);
                }
                BukkitCompat.showTitle(msgMe, "", "", 0, Integer.MAX_VALUE, 0);
            } else {
                Player msgMe = Bukkit.getPlayer(player.getKey());
                // return nextSwapTick - (Bukkit.getCurrentTick() - SwapPlugin.get().getGameManager().startTick);
                Long time =  (playerInBody.getNextSwapIn(player.getKey()) - (Bukkit.getCurrentTick() - SwapPlugin.get().getGameManager().startTick) ) / 20;
                String msg = String.format("§eSwap in: §c%ds", time);
                //ActionBarUtil.sendActionBar(msgMe, msg);
                updateSwapIn(player.getKey(), msg);
                updateBarPlayer(player.getKey());

                String t = "§6§lYou Are Swapped Out!";
                BukkitCompat.showTitle(msgMe, t, "", 0, Integer.MAX_VALUE, 0);


            }
        }
    }


}
