package com.jeremyseq.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(com.jeremyseq.DamageIndicators.MOD_ID)
public final class DamageIndicators {
    public DamageIndicators(IEventBus modBus) {
        com.jeremyseq.DamageIndicators.init();
    }
}