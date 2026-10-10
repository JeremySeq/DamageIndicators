package dev.jeremyseq.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(dev.jeremyseq.DamageIndicators.MOD_ID)
public final class DamageIndicators {
    public DamageIndicators(IEventBus modBus) {
        dev.jeremyseq.DamageIndicators.init();
    }
}