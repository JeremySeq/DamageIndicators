package com.jeremyseq.neoforge;

import com.jeremyseq.DamageIndicators;
import com.jeremyseq.events.ClientTickHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = DamageIndicators.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientTickHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientTickHandler.tick();
    }
}