package com.shard.generalswap.state;

import com.shard.generalswap.body.Body;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerInBody {
    private final Map<UUID, Body> playerToBody;

    public PlayerInBody(List<UUID> allPlayers) {
        playerToBody = new HashMap<>();
        for (UUID player : allPlayers) {
            if (player == null) {
                System.out.println("WHTF??");

            }
            playerToBody.put(player, null); // null meaning they are swapped out rn
        }
    }

    public Set<Map.Entry<UUID, Body>> get() {
        return playerToBody.entrySet();
    }

    public void switchBody(UUID player, Body body) {
        playerToBody.put(player, body);
    }

    public boolean isSwappedOut(UUID player) {
        return playerToBody.get(player) == null;
    }

    public boolean isSwappedIn(UUID player) {
        return playerToBody.get(player) != null;
    }

    public Body getBody(UUID player) {
        return playerToBody.get(player);
    }
}
