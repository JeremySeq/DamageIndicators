package dev.jeremyseq.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(dev.jeremyseq.DamageIndicators.MOD_ID)
public final class DamageIndicators {
    public DamageIndicators(FMLJavaModLoadingContext context) {
        dev.jeremyseq.DamageIndicators.init();
    }
}
