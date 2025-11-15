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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final SwapPlugin plugin;
    private final SwapOrchestrator orchestrator;
    private  List<BodyController> controllers;
    private final Configuration config;
    private final InactiveManager inactiveManager;
    public PlayerInBody playerInBody;
    boolean started = false;

    public GameManager(SwapPlugin p, SwapOrchestrator o, Configuration c, InactiveManager inactiveManager) {
        plugin = p;
        orchestrator = o;
        config = c;
        controllers = new ArrayList<>();
        this.inactiveManager = inactiveManager;
    }

    public boolean gameStarted() {
        return started;
    }

    public boolean isSwappedOut(Player player) {
        return inactiveManager.isPlayerInactive(player);
    }

    public void start() {
        // is every player online?
        started = true;
        List<Player> players = new ArrayList<>();

        for (String name : config.players()) {
            Player player = Bukkit.getPlayer(name);
            if (player == null) {
                System.err.println("Player: " + name + " was not found");
                return;
            } else {
                players.add(player);
            }
        }
        playerInBody = new PlayerInBody(players);

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

