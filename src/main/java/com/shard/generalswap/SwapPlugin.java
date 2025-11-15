package com.shard.generalswap;

import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.SwapStage;
import com.shard.generalswap.game.SwapOrchestrator;
import com.shard.generalswap.state.PlayerRegistry;
import com.shard.generalswap.util.ConfigLoader;
import com.shard.generalswap.util.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SwapPlugin extends JavaPlugin {

    private static SwapPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Swap plugin enabled.");

        // Load config
        Map<String, List<SwapStage>> bodies;
        List<String> players;
        try {
            Configuration config = ConfigLoader.loadBodies(new File(getDataFolder(), "config.conf"));
            bodies = config.bodies();
            players = config.players();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        PlayerRegistry playerRegistry = new PlayerRegistry(players);

        SwapOrchestrator orchestrator = new SwapOrchestrator(playerRegistry);

        // Create controllers
        int i = 1;
        for (Map.Entry<String, List<SwapStage>> entry : bodies.entrySet()) {
            BodyController controller = new BodyController(
                    i,
                    entry.getKey(),
                    entry.getValue(),
                    orchestrator,
                    playerRegistry
            );
            controller.start();
            i++;
        }

        // Tick the orchestrator
        Bukkit.getScheduler().runTaskTimer(this, orchestrator::tick, 1L, 1L);
    }

    public static SwapPlugin get() { return instance; }
}
