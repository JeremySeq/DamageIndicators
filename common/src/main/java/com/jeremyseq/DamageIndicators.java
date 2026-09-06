package com.jeremyseq;

import com.jeremyseq.config.DamageIndicatorsConfig;
import com.jeremyseq.events.ConfigReloader;
import com.jeremyseq.events.DamageHandler;
import com.jeremyseq.overlays.IndicatorOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DamageIndicators {
    public static final String MOD_ID = "jeremyseqsdamageindicators";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        DamageIndicatorsConfig.load();
        ConfigReloader.register();
        DamageHandler.register();
        IndicatorOverlay.setup();
        // Write common init code here.
    }
}
