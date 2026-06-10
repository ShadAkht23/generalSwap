package com.shard.generalswap;

import com.shard.generalswap.commands.StartCommand;
import com.shard.generalswap.game.GameManager;
import com.shard.generalswap.game.InactiveManager;
import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.game.Visualizer;
import com.shard.generalswap.listeners.EventListeners;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.util.ConfigLoader;
import com.shard.generalswap.util.Configuration;
import de.maxhenkel.voicechat.api.VoicechatApi;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/*
TODO
    immediate start so other runner doesn't know the spawn
    Hiding Chat
    Timer for Swapped out players _/
    manhunt compass
    does ender chest work? respawn anchor? pet owning?
    cage without visible bedrock? _/
    handle end portal thing
    update hostile mob targetting
    fix endermen mob targetting





 */
import de.maxhenkel.voicechat.api.VoicechatPlugin;


public class SwapPlugin extends JavaPlugin {

    private static SwapPlugin instance;

    private GameManager gameManager;

    private VoicechatApi voicechatServerApi;

    public void start() {
        gameManager.start();
    }
    public void stop() {gameManager.stop();}

    @Override
    public void onEnable() {
        instance = this;

        // Load config
        Configuration config;
        try {
            config = ConfigLoader.loadBodies(new File(getDataFolder(), "config.conf"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        InactiveManager inactiveManager = new InactiveManager();
        PlayerInBody playerInBody = new PlayerInBody();
        SwapOrchestrator orchestrator = new SwapOrchestrator(inactiveManager, playerInBody);

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
        Visualizer visualizer = new Visualizer(inactiveManager, playerInBody);
        gameManager = new GameManager(this, orchestrator, config, inactiveManager, playerInBody, visualizer);
        getServer().getPluginManager().registerEvents(new EventListeners(gameManager, playerInBody, orchestrator, visualizer), this);
        getLogger().info("Swap plugin enabled.");
    }

    public void setVoicechatServerApi(VoicechatApi vcplugin) {
        voicechatServerApi = vcplugin;
    }

    public static SwapPlugin get() { return instance; }
    public GameManager getGameManager() {return gameManager; }
}
