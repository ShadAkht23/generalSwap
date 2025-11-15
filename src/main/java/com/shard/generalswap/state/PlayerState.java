package com.shard.generalswap.state;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class PlayerState {

    private final Location loc;
    private final ItemStack[] contents;

    public PlayerState(Player player) {
        this.loc = player.getLocation().clone();
        this.contents = player.getInventory().getContents().clone();
    }

    public void applyTo(Player player) {
        player.teleport(loc);
        player.getInventory().setContents(contents);
    }
}
