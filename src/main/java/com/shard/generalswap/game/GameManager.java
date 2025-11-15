package com.shard.generalswap.game;

import com.shard.generalswap.SwapPlugin;
import com.shard.generalswap.body.Body;
import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.SwapStage;
import com.shard.generalswap.util.ConfigSwapStage;
import com.shard.generalswap.util.Configuration;
import jdk.management.jfr.ConfigurationInfo;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final SwapPlugin plugin;
    private final SwapOrchestrator orchestrator;
    private  List<BodyController> controllers;
    private final Configuration config;

    public GameManager(SwapPlugin p, SwapOrchestrator o, Configuration c) {
        plugin = p;
        orchestrator = o;
        config = c;
        controllers = new ArrayList<>();
    }

    public void start() {
        // is every player online?
        for (String name : config.players()) {
            if (Bukkit.getPlayer(name) == null) {
                System.err.println("Player: " + name + " was not found");
            }
        }
        for (Map.Entry<String, List<ConfigSwapStage>> css : config.bodies().entrySet()) {
            List<SwapStage> swapStages = new ArrayList<>();
            for (ConfigSwapStage ss : css.getValue()) {
                swapStages.add(new SwapStage(Bukkit.getPlayer(ss.playerName()), ss.durationTicks()));
            }
            BodyController controller = new BodyController(null, css.getKey(), swapStages, orchestrator);
            controller.start();
            controllers.add(controller);
        }
        Bukkit.getScheduler().runTaskTimer(plugin, orchestrator::tick, 1L, 1L);
    }

}

