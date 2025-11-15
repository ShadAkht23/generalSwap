package com.shard.generalswap.body;

public record SwapEvent(
        String playerIn,
        String playerOut,
        Body state,
        BodyController callBack
) {}
