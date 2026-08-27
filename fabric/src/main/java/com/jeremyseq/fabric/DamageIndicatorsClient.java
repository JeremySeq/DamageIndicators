package com.jeremyseq.fabric;

import net.fabricmc.api.ClientModInitializer;

public final class DamageIndicatorsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricRenderEvents.register();
        FabricClientTickHandler.register();
    }
}
