package com.shard.generalswap.body;

import java.util.UUID;

public record SwapEvent(
        UUID playerIn,
        UUID playerOut,
        Body state,
        BodyController callBack
) {}
