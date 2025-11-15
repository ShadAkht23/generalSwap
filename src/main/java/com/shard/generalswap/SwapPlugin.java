package com.shard.generalswap;

import com.shard.generalswap.commands.StartCommand;
import com.shard.generalswap.game.GameManager;
import com.shard.generalswap.game.InactiveManager;
import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.listeners.EventListeners;
import com.shard.generalswap.util.ConfigLoader;
import com.shard.generalswap.util.ConfigSwapStage;
import com.shard.generalswap.util.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SwapPlugin extends JavaPlugin {

    private static SwapPlugin instance;

    private GameManager gameManager;

    public void start() {
        gameManager.start();
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Swap plugin enabled.");

        // Load config
        Map<String, List<ConfigSwapStage>> bodies;
        List<String> players;
        Configuration config;
        try {
            config = ConfigLoader.loadBodies(new File(getDataFolder(), "config.conf"));
            bodies = config.bodies();
            players = config.players();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        InactiveManager inactiveManager = new InactiveManager();
        SwapOrchestrator orchestrator = new SwapOrchestrator(inactiveManager);


        StartCommand startCommand = new StartCommand(this);
        try {
            if (getCommand("gswap") != null) {
                getCommand("gswap").setExecutor(startCommand);
                getCommand("gswap").setTabCompleter(startCommand);
                getLogger().info("Successfully registered /gswap command");
            } else {
                getLogger().severe("Failed to register /gswap command - command not found in plugin.yml");
            }
        } catch (Exception e) {
            getLogger().severe("Error registering /gswap command: " + e.getMessage());
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(new EventListeners(), this);
        gameManager = new GameManager(this, orchestrator, config, inactiveManager);
    }

    public static SwapPlugin get() { return instance; }
    public GameManager getGameManager() {return gameManager; }
}
