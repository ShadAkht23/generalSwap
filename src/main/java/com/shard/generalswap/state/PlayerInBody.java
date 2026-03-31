package com.shard.generalswap.state;

import com.shard.generalswap.body.Body;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

import java.util.*;



// represents which body the player SHOULD be in.
// also keeps track of whether each player has actually been swapped in or not.
public class PlayerInBody {
    private final Map<UUID, Body> playerToBody;
    private final Map<UUID, Boolean> stateApplied;
    private final Map<EnderPearl, UUID> pendingPearlSwap;

    public PlayerInBody(List<UUID> allPlayers) {
        playerToBody = new HashMap<>();
        stateApplied = new HashMap<>();
        pendingPearlSwap = new HashMap<>();
        for (UUID player : allPlayers) {
            if (player == null) {
                System.out.println("WHTF??");

            }
            playerToBody.put(player, null); // null meaning they are swapped out rn
            stateApplied.put(player, true);
        }
    }

    public void clear() {
        playerToBody.clear();
        stateApplied.clear();
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
