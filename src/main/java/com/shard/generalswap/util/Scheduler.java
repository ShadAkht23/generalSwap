package com.shard.generalswap.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Scheduler {

    private final Plugin plugin;

    public Scheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void later(long ticks, Runnable r) {
        Bukkit.getScheduler().runTaskLater(plugin, r, ticks);
    }
}
