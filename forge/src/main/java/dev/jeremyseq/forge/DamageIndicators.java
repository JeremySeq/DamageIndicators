package dev.jeremyseq.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(dev.jeremyseq.DamageIndicators.MOD_ID)
public final class DamageIndicators {
    public DamageIndicators(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) return;
        dev.jeremyseq.DamageIndicators.init();
    }
}
