package com.shard.generalswap.util;

import com.shard.generalswap.body.BodyController;
import com.shard.generalswap.body.SwapStage;

import java.util.List;
import java.util.Map;

public record Configuration(Map<String, BodyConfig> bodies, List<String> players) {}
