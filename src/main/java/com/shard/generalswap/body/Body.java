package com.shard.generalswap.body;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.state.PlayerState;
import com.shard.generalswap.util.PlayerStateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


// hunter state: runnerIdx
// runner state:

// state of -1 = swapped out
// state of 0 = empty state
public class Body {

    private final String name;
    //private final String id;

    private PlayerState state;

    private Location spawn;
    public long nextSwapTick = 0;

    private Body(String n) {
        name = n;
    }
    private Role role;
    private int runnerIdx = 0;

    public static Body SwappedOutBody() {
        return new Body("SWAPPED OUT");
    }

    public Body(PlayerState startingState, String n, Role r) {
        //this.id = id;
        //this.location = baseLocation;
        if (startingState != null) {
            state = startingState;
        }
        spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
        name = n;
        role = r;
    }

   // public String getId() { return id; }
    //public Location getLocation() { return location; }

    public void setNextSwapTick(long s) {
        nextSwapTick = s;
    }

    public long ticksTillNextSwap() {
        if (nextSwapTick != -1)
            return nextSwapTick - (Bukkit.getCurrentTick() - SwapPlugin.get().getGameManager().startTick);
        else return -1;
    }


    // will get the player then apply the state.
    public void applyPlayerState(@NotNull Player player) {
        // assumes player is online
        player.setRespawnLocation(spawn);
        PlayerStateUtil.applyPlayerState(player, state);
        System.out.println("setting respawn location: " + spawn.toString() + "for player: " + player.getName());
    }

    public void setSpawn(Location loc) {
        spawn = loc;
        System.out.println("setting spawn: " + loc.toString() + " for body: " + name);
    }

    public void setPearlLand(Location loc) {
        state.setPearlLandLoc(loc);
    }

    public void set(PlayerState s) {
        state = s;
    }
    /*public int get() {
        return state;
    }*/

    public String getName() {return name;}


    public boolean isHunter() {return role == Role.HUNTER;}
    public boolean isRunner() {return role == Role.RUNNER;}

    /*public void updateLocation(Location newLoc) {
        this.location = newLoc;
    }*/
}
