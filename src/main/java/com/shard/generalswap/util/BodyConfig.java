package com.shard.generalswap.util;

import com.shard.generalswap.body.Role;

import java.util.List;

public record BodyConfig(Role role, List<ConfigSwapStage> stages) {}

