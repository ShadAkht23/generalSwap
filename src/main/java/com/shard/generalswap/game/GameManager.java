package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.SwapStage;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.util.BodyConfig;
import com.shard.generalswap.util.ConfigSwapStage;
import com.shard.generalswap.util.Configuration;
import com.shard.generalswap.util.PlayerStateUtil;
import jdk.management.jfr.ConfigurationInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final SwapPlugin plugin;
    private final SwapOrchestrator orchestrator;
    private  List<BodyController> controllers;
    private final Configuration config;
    private final InactiveManager inactiveManager;
    private final Visualizer visualizer;
    private BukkitTask curTickTask;

    public PlayerInBody playerInBody;
    public long startTick = 0;
    boolean started = false;

    public GameManager(SwapPlugin p, SwapOrchestrator o, Configuration c, InactiveManager inactiveManager, PlayerInBody playerInBody, Visualizer visualizer) {
        plugin = p;
        orchestrator = o;
        config = c;
        controllers = new ArrayList<>();
        this.inactiveManager = inactiveManager;
        this.visualizer = visualizer;
        this.playerInBody = playerInBody;
    }



    public boolean gameStarted() {
        return started;
    }

    public boolean isSwappedOut(UUID player) {
        return playerInBody.isSwappedOut(player);
    }

    public void start() {
        // is every player online?
        started = true;
        List<UUID> players = new ArrayList<>();

        for (String name : config.players()) {
            Player player = Bukkit.getPlayer(name);
            if (player == null) {
                System.err.println("Player: " + name + " was not found");
                return;
            } else {
                players.add(player.getUniqueId());
            }
        }
        playerInBody.initialize(players);

        Set<UUID> swappedIn = new HashSet<>();

        for (Map.Entry<String, BodyConfig> css : config.bodies().entrySet()) {
            List<SwapStage> swapStages = new ArrayList<>();
            for (ConfigSwapStage ss : css.getValue().stages()) {
                swapStages.add(new SwapStage(Bukkit.getPlayer(ss.playerName()).getUniqueId(), ss.durationTicks()));
            }
            BodyController controller = new BodyController(null, css.getKey(), swapStages, css.getValue().role() , orchestrator);
            controller.start();
            swappedIn.add(controller.currentHost());
            Player player = Bukkit.getPlayer(controller.currentHost());
            if (controller.getBody().isHunter()) {
                player.give(new ItemStack(Material.COMPASS));
            }
            controller.getBody().set(PlayerStateUtil.capturePlayerState(player));
            controllers.add(controller);
        }
        players.removeAll(swappedIn);
        for (UUID inactive : players) {
            Player player = Bukkit.getPlayer(inactive);
            VoiceChannelSwapper.swapOut(inactive);
            if (player != null) {
                inactiveManager.makeInactive(player);
            } else {
                System.out.println("player disconnected mid tick....");
            }
        }

        startTick = Bukkit.getCurrentTick() + 1;
        curTickTask = Bukkit.getScheduler().runTaskTimer(plugin, orchestrator::tick, 1L, 1L);
        visualizer.startActionBarUpdates();
    }

    public boolean isPlaying(UUID player) {
        if (playerInBody.getBody(player) == null) {
            return false;
        }
        return true;
    }

    public void stop() {
        if (!started) {
            System.err.println("Cannot stop game that hasn't started");
            return;
        }
        started = false;

        playerInBody.clear();
        curTickTask.cancel();
        visualizer.stopActionBarUpdates();
        for (Player player : Bukkit.getOnlinePlayers()) {
            inactiveManager.makeActive(player);
        }
        getOrchestrator().reset();
    }

    public SwapOrchestrator getOrchestrator() {
        return orchestrator;
    }

    public Visualizer getVisualizer() {return visualizer;}

    public InactiveManager getInactiveManager() {return inactiveManager;}

    public List<Body> getHunters() {
        return controllers.stream()
                .map(BodyController::getBody)
                .filter(Body::isHunter)
                .toList();
    }

    public List<BodyController> getRunners() {
        return controllers.stream()
                .filter(c -> c.getBody().isRunner())
                .toList();
    }

    public boolean isHunterRn(UUID player) {
        for (BodyController controller : controllers) {
            if (controller.getBody().isHunter() && controller.currentHost() == player) {
                return true;
            }
        }
        return false;
    }

    public boolean isRunnerRn(UUID player) {
        for (BodyController controller : controllers) {
            if (controller.getBody().isRunner() && controller.currentHost() == player) {
                return true;
            }
        }
        return false;
    }
}

