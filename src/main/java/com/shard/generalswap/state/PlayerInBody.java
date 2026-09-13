package com.shard.generalswap.state;

import com.shard.generalswap.body.ActiveBody;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.BodyAssignment;
import com.shard.generalswap.body.SwappedOut;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

import java.util.*;



// represents which body the player SHOULD be in.
// also keeps track of whether each player has actually been swapped in or not.
public class PlayerInBody {
    private final Map<UUID, BodyAssignment> playerToBody;
    private final Map<UUID, Boolean> stateApplied;
    private final Map<EnderPearl, UUID> pendingPearlSwap;
    private final Map<UUID, Long> nextSwapIn;

    public PlayerInBody() {
        playerToBody = new HashMap<>();
        stateApplied = new HashMap<>();
        pendingPearlSwap = new HashMap<>();
        nextSwapIn = new HashMap<>();
    }

    public void clear() {
        playerToBody.clear();
        stateApplied.clear();
        pendingPearlSwap.clear();
        nextSwapIn.clear();
    }

    public void initialize(List<UUID> allPlayers) {
        clear();
        for (UUID player : allPlayers) {
            if (player == null) {
                System.out.println("WHTF??");
            }
            swapOut(player);
            stateApplied.put(player, true);
        }
    }

    public void updateNextSwapIn(UUID player, Long delay) {
        nextSwapIn.put(player, delay);
    }
    public Long getNextSwapIn(UUID player) {
        return nextSwapIn.get(player);
    }



    public void addPendingPearlSwap(EnderPearl enderPearl, UUID player) {
        pendingPearlSwap.put(enderPearl, player);
    }

    public UUID getPendingEnderPearlSwap(EnderPearl enderPearl) {
        return pendingPearlSwap.get(enderPearl); // may return null meaning the owner of the pearl wasn't swapped
    }

    public void pearlNotPending(EnderPearl enderPearl) {
        pendingPearlSwap.remove(enderPearl);
    }

    public void appliedState(UUID player) {stateApplied.put(player, true); }

    public void applyStateLater(UUID player) {stateApplied.put(player, false); }

    public boolean isStateApplied(UUID player) {return stateApplied.get(player); }

    public Set<Map.Entry<UUID, BodyAssignment>> get() {
        return playerToBody.entrySet();
    }

    public void switchBody(UUID player, Body body) {
        playerToBody.put(player, new ActiveBody(body));
    }

    public void swapOut(UUID player) {
        playerToBody.put(player, new SwappedOut());
    }

    public boolean isSwappedOut(UUID player) {
        return playerToBody.get(player) instanceof SwappedOut;
    }

    public boolean isSwappedIn(UUID player) {
        return !isSwappedOut(player);
    }

    public BodyAssignment getBodyAssignment(UUID player) {
        return playerToBody.get(player);
    }
    
    public Body getBody(UUID player) {
        return switch (playerToBody.get(player)) {
            case ActiveBody(Body body) -> body;
            case SwappedOut swappedOut -> throw new IllegalStateException("HOW is the player swapped out???");
        };
    }
}
