package com.jeremyseq.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(com.jeremyseq.DamageIndicators.MOD_ID)
public final class DamageIndicators {
    public DamageIndicators(FMLJavaModLoadingContext context) {
        com.jeremyseq.DamageIndicators.init();
    }
}
