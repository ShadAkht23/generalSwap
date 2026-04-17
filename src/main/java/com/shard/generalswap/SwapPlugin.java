package com.shard.generalswap;

import com.shard.generalswap.commands.StartCommand;
import com.shard.generalswap.game.GameManager;
import com.shard.generalswap.game.InactiveManager;
import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.listeners.EventListeners;
import com.shard.generalswap.util.ConfigLoader;
import com.shard.generalswap.util.ConfigSwapStage;
import com.shard.generalswap.util.Configuration;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;

/*
TODO
    immediate start so other runner doesn't know the spawn
    Hiding Chat
    Timer for Swapped out players _/
    manhunt compass
    does ender chest work? respawn anchor? pet owning?
    cage without visible bedrock?
    handle end portal thing
    update hostile mob targetting
    fix endermen mob targetting





 */
import de.maxhenkel.voicechat.api.VoicechatPlugin;


public class SwapPlugin extends JavaPlugin {

    private static SwapPlugin instance;

    private GameManager gameManager;

    private Plugin manhuntPlus;

    private VoicechatApi voicechatServerApi;

    public void start() {
        gameManager.start();
    }
    public void stop() {gameManager.stop();}

    @Override
    public void onEnable() {
        instance = this;
        if (Bukkit.getPluginManager().getPlugin("ManhuntPlus") != null) {
            manhuntPlus = Bukkit.getPluginManager().getPlugin("ManhuntPlus");
            getLogger().info("Successfully hooked into Manhunt+!");
        }

        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(new VoiceChatPlugin(this));
            System.out.println("Successfully registered voice chat plugin");
        }

        // Load config
        Configuration config;
        try {
            config = ConfigLoader.loadBodies(new File(getDataFolder(), "config.conf"));
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
        getLogger().info("Swap plugin enabled.");
    }

    public void setVoicechatServerApi(VoicechatApi vcplugin) {
        voicechatServerApi = vcplugin;
    }


    public static SwapPlugin get() { return instance; }
    public GameManager getGameManager() {return gameManager; }
}
