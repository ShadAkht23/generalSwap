package com.shard.generalswap.state;

import java.util.*;

// probably won't need this when we switch to the real state.
public class PlayerRegistry {

    // make it use PlayerState instead of int
    public PlayerRegistry(List<String> names) {
        states = new HashMap<>();
        for (String name : names) {
            states.put(name, -1);
        }
    }

    private final Map<String, Integer> states;

    public void save(String name, Integer state) {
        states.put(name, state);
    }

    public Integer get(String name) {
        return states.get(name);
    }
    public void print() {
        for (Map.Entry<String, Integer> entry : states.entrySet()) {
            System.out.println(entry.getKey() + " has state " + entry.getValue().toString());
        }
    }

}
