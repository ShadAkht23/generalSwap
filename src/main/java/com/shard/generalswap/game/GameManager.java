package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.SwapStage;
import com.shard.generalswap.state.PlayerInBody;
import com.shard.generalswap.util.ConfigSwapStage;
import com.shard.generalswap.util.Configuration;
import jdk.management.jfr.ConfigurationInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GameManager {

    private final SwapPlugin plugin;
    private final SwapOrchestrator orchestrator;
    private  List<BodyController> controllers;
    private final Configuration config;
    private final InactiveManager inactiveManager;
    private final Visualizer visualizer;
    public PlayerInBody playerInBody;
    public long startTick = 0;
    boolean started = false;

    public GameManager(SwapPlugin p, SwapOrchestrator o, Configuration c, InactiveManager inactiveManager) {
        plugin = p;
        orchestrator = o;
        config = c;
        controllers = new ArrayList<>();
        this.inactiveManager = inactiveManager;
        visualizer = new Visualizer();
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
        playerInBody = new PlayerInBody(players);

        Set<UUID> swappedIn = new HashSet<>();

        for (Map.Entry<String, List<ConfigSwapStage>> css : config.bodies().entrySet()) {
            List<SwapStage> swapStages = new ArrayList<>();
            for (ConfigSwapStage ss : css.getValue()) {
                swapStages.add(new SwapStage(Bukkit.getPlayer(ss.playerName()).getUniqueId(), ss.durationTicks()));
            }
            BodyController controller = new BodyController(null, css.getKey(), swapStages, orchestrator);
            controller.start();
            swappedIn.add(controller.currentHost());
            controllers.add(controller);
        }
        players.removeAll(swappedIn);
        for (UUID inactive : players) {
            Player player = Bukkit.getPlayer(inactive);
            if (player != null) {
                inactiveManager.makeInactive(player);
            } else {
                System.out.println("player disconnected mid tick....");
            }
        }

        startTick = Bukkit.getCurrentTick() + 1;
        Bukkit.getScheduler().runTaskTimer(plugin, orchestrator::tick, 1L, 1L);
        visualizer.startActionBarUpdates();
    }

    public SwapOrchestrator getOrchestrator() {
        return orchestrator;
    }
}

