package com.shard.generalswap.state;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PendingChatMsgs {
    private final Map<UUID, List<Component>> pendingChatMsgs;

    public PendingChatMsgs() {
        pendingChatMsgs = new HashMap<>();
    }

    public void clear() {
        pendingChatMsgs.clear();
    }

    public void appendPendingChatMsg(UUID player, Component msg) {
        pendingChatMsgs.computeIfAbsent(player, k -> new ArrayList<>()).add(msg);
    }

    public void emptyPendingChatMsgs(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            if (pendingChatMsgs.containsKey(uuid)) {
                List<Component> msgs = pendingChatMsgs.get(uuid);
                for (Component msg : msgs) {
                    player.sendMessage(msg);
                }
                msgs.clear();
            }
        }
    }
}
