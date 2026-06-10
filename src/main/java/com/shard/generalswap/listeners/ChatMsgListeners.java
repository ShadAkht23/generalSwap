package com.shard.generalswap.listeners;

import com.shard.generalswap.game.GameManager;
import com.shard.generalswap.state.PendingChatMsgs;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class ChatMsgListeners implements Listener {
    private final GameManager gameManager;
    PendingChatMsgs pendingChatMsgs;

    public ChatMsgListeners(GameManager gameManager, PendingChatMsgs pendingChatMsgs) {
        this.gameManager = gameManager;
        this.pendingChatMsgs = pendingChatMsgs;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Component msg = event.deathMessage();
        if (msg == null)
            return;
        if (!gameManager.gameStarted()) {
            return;
        }

        event.setShowDeathMessages(false);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameManager.isSwappedOut(player.getUniqueId())) {
                pendingChatMsgs.appendPendingChatMsg(player.getUniqueId(), msg);
            } else {
                player.sendMessage(msg);
            }
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (!gameManager.gameStarted()) {
            return;
        }
        Component msg = event.message();
        Component empty = Component.empty();
        event.message(null);
        if (msg == null)
            return;
        if (gameManager.gameStarted()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (gameManager.isSwappedOut(player.getUniqueId())) {
                    // only add if they are about to get swapped into the player who owns this body??
                    pendingChatMsgs.appendPendingChatMsg(player.getUniqueId(), msg);
                } else {
                    if (msg.toString().length() < 5)
                        return;
                    player.sendMessage(msg);
                }
            }
        }
    }
}
