package com.shard.generalswap.state;

import com.shard.generalswap.body.Body;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerInBody {
    private final Map<Player, Body> playerToBody;

    public PlayerInBody(List<Player> allPlayers) {
        playerToBody = new HashMap<>();
        for (Player player : allPlayers) {
            playerToBody.put(player, null); // null meaning they are swapped out rn
        }
    }

    public void switchBody(Player player, Body body) {
        playerToBody.put(player, body);
    }

    public boolean isSwappedOut(Player player) {
        return playerToBody.get(player) == null;
    }

    public boolean isSwappedIn(Player player) {
        return playerToBody.get(player) != null;
    }

    public Body getBody(Player player) {
        return playerToBody.get(player);
    }
}
