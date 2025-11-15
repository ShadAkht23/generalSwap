package com.shard.generalswap.body;

import java.util.HashMap;
import java.util.Map;

public class BodyRegistry {

    // mapping from player to body
    private final Map<String, Body> bodies = new HashMap<>();

    public void register(String playerName, Body body) {
        bodies.put(playerName, body);
    }

    public Body get(String playerName) {
        return bodies.get(playerName);
    }
}
