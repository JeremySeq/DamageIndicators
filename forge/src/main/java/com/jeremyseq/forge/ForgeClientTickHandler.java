package com.jeremyseq.forge;

import com.jeremyseq.DamageIndicators;
import com.jeremyseq.events.ClientTickHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DamageIndicators.MOD_ID, value = Dist.CLIENT)
public class ForgeClientTickHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientTickHandler.tick();
        }
    }
}