package com.shard.generalswap.body;

import org.bukkit.Location;
import org.bukkit.entity.Player;


// state of -1 = swapped out
// state of 0 = empty state
public class Body {

    private final String name;
    //private final String id;
    private Location location;
    private int state;

    public Body(int startingState, String n) {
        //this.id = id;
        //this.location = baseLocation;
        state = startingState;
        name = n;
    }

   // public String getId() { return id; }
    //public Location getLocation() { return location; }

    public void applyPlayerState(Player player) {
        player.teleport(location);
    }

    public void set(int s) {
        state = s;
    }
    public int get() {
        return state;
    }


    /*public void updateLocation(Location newLoc) {
        this.location = newLoc;
    }*/
}
