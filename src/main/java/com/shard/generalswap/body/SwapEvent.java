package com.shard.generalswap.body;

import org.bukkit.entity.Player;

public record SwapEvent(
        Player playerIn,
        Player playerOut,
        Body state,
        BodyController callBack
) {}
