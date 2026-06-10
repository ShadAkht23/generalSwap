package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.util.ActionBarUtil;
import com.shard.generalswap.util.BukkitCompat;
import com.shard.generalswap.util.Colours;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.title.Title.title;

public class Visualizer {

    private BukkitTask actionBarTask;
    private final Map<UUID, ActionBarTexts> barTexts;

    private final InactiveManager inactiveManager;
    private final PlayerInBody playerInBody;


    public Visualizer(InactiveManager inactiveManager, PlayerInBody playerInBody) {
        barTexts = new HashMap<>();
        this.inactiveManager = inactiveManager;
        this.playerInBody = playerInBody;
    }

    public void startActionBarUpdates() {
        actionBarTask = Bukkit.getScheduler().runTaskTimer(SwapPlugin.get(), this::updateActionBar, 3L, 20);
    }

    public void stopActionBarUpdates() {
        actionBarTask.cancel();
        for (UUID uuid: inactiveManager.getCagedPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                BukkitCompat.showTitle(player, "", "", 0, Integer.MAX_VALUE, 0);
            }
        }
    }
    private void updateSwapIn(UUID uuid, TextComponent msg) {
        ActionBarTexts texts = barTexts.get(uuid);
        if (texts == null) {
            texts = new ActionBarTexts();
        }
        texts.nextSwapIn = msg;
        barTexts.put(uuid, texts);
    }

    private void updateTracking(UUID uuid, TextComponent msg) {
        ActionBarTexts texts = barTexts.get(uuid);
        if (texts == null) {
            texts = new ActionBarTexts();
        }
        texts.trackingMsg = msg;
        texts.trackingTick = Bukkit.getCurrentTick();
        barTexts.put(uuid, texts);
    }

    private void updateBarPlayer(UUID uuid) {
        ActionBarTexts texts = barTexts.get(uuid);
        if (texts == null)
            return;
        TextComponent msg = Component.empty();
        boolean empty = true;
        if (texts.nextSwapIn != null) {
            msg = msg.append(texts.nextSwapIn);
            empty = false;
        }
        if (texts.trackingMsg != null) {
            if (Bukkit.getCurrentTick() - texts.trackingTick < 40) {
                if (!empty) {
                    msg = msg.append(Component.text("  |  ").color(Colours.Aqua));
                }
                msg = msg.append(texts.trackingMsg);
            } else {
                texts.trackingMsg = null;
            }
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendActionBar(msg);
        }
    }

    public void trackingUpdateSuccess(Player hunter, String runnerName) {
        TextComponent msg = Component.text("Tracking "  + runnerName).color(Colours.Green);
        updateTracking(hunter.getUniqueId(), msg);
        updateBarPlayer(hunter.getUniqueId());
    }

    public void trackingUpdateBadDimension(Player hunter, String runnerName) {
        TextComponent msg = Component.text(runnerName + " is not in your dimension").color(Colours.Red);
        updateTracking(hunter.getUniqueId(), msg);
        updateBarPlayer(hunter.getUniqueId());
    }

    private void updateActionBar() {
        for (Map.Entry<UUID, Body> player : playerInBody.get()) {
            if (player.getValue() == null)
                continue;
            if (!player.getValue().getName().equals("SWAPPED OUT")) {
                long timeLeft = player.getValue().ticksTillNextSwap();
                TextComponent msg;
                Player msgMe = Bukkit.getPlayer(player.getKey());
                if (timeLeft == -1) {
                    msg = Component.text("No Swap").color(Colours.Yellow);
                } else {
                    msg = Component.text("Swap in: ").color(Colours.Yellow)
                                    .append(Component.text(Math.max(0, timeLeft) / 20).color(Colours.Red));
                    updateSwapIn(player.getKey(), msg);
                    updateBarPlayer(player.getKey());
                    //ActionBarUtil.sendActionBar(msgMe, msg);
                }
                BukkitCompat.showTitle(msgMe, "", "", 0, Integer.MAX_VALUE, 0);
            } else {
                Player msgMe = Bukkit.getPlayer(player.getKey());
                Long time =  (playerInBody.getNextSwapIn(player.getKey()) - (Bukkit.getCurrentTick() - SwapPlugin.get().getGameManager().startTick) ) / 20;
                TextComponent msg = Component.text("Swap in: ").color(Colours.Yellow).append(Component.text(time).color(Colours.Red));
                updateSwapIn(player.getKey(), msg);
                updateBarPlayer(player.getKey());
                if (msgMe != null)  msgMe.showTitle(title(Component.text("You are Swapped Out!").color(Colours.Gold), Component.empty(), 0, 20, 0));
            }
        }
    }


}
