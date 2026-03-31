package com.shard.generalswap.util;

import com.shard.generalswap.body.SwapStage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ConfigLoader {


    public static Configuration loadBodies(File file) throws IOException {
        Map<String, List<ConfigSwapStage>> result = new HashMap<>();
        Set<String> players = new HashSet<>();
        for (String line : Files.readAllLines(file.toPath())) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            // bodyName = [A:60, B:30]
            String[] parts = line.split("=", 2);
            String bodyName = parts[0].trim();
            String right = parts[1].trim();

            ImmutablePair<List<ConfigSwapStage>, Set<String>> stages = parseStages(right);
            result.put(bodyName, stages.left);
            players.addAll(stages.right);
        }

        return new Configuration(result, new ArrayList<>(players));
    }

    private static ImmutablePair<List<ConfigSwapStage>, Set<String>> parseStages(String text) {
        List<ConfigSwapStage> list = new ArrayList<>();
        Set<String> players = new HashSet<>();

        // Remove outer whitespace
        text = text.trim();

        // Split like "[A:60, B:30]" by brackets
        // This regex finds each "[...]"
        Matcher m = Pattern.compile("\\[(.*?)]").matcher(text);

        while (m.find()) {
            String inside = m.group(1);  // e.g. "A:60, B:30" or "A:60"
            for (String part : inside.split(",")) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) continue;

                String[] kv = trimmed.split(":");
                String player = kv[0].trim();
                long seconds = Long.parseLong(kv[1].trim());


                players.add(player);
                list.add(new ConfigSwapStage(player, seconds * 20));
            }
        }

        return new ImmutablePair<>(list, players);
    }
}
