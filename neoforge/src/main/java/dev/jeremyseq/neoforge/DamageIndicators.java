package dev.jeremyseq.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(dev.jeremyseq.DamageIndicators.MOD_ID)
public final class DamageIndicators {
    public DamageIndicators(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) return;
        dev.jeremyseq.DamageIndicators.init();
    }
}