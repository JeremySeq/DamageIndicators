package com.jeremyseq.events;

import com.jeremyseq.config.DamageIndicatorsConfig;

public class ConfigReloader {
    private static int reloadCheckCounter = 0;

    public static void register() {
        ClientTickHandler.register(ConfigReloader::onClientTick);
    }

    private static void onClientTick() {
        reloadCheckCounter++;
        if (reloadCheckCounter >= 20) { // once per second at 20 TPS
            reloadCheckCounter = 0;
            DamageIndicatorsConfig.reloadIfChanged();
        }
    }

}
