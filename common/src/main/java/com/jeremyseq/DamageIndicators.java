package com.jeremyseq;

import com.jeremyseq.config.DamageIndicatorsConfig;
import com.jeremyseq.events.DamageHandler;
import com.jeremyseq.overlays.IndicatorOverlay;

public final class DamageIndicators {
    public static final String MOD_ID = "jeremyseqsdamageindicators";

    public static void init() {
        DamageIndicatorsConfig.load();
        DamageHandler.register();
        IndicatorOverlay.setup();
        // Write common init code here.
    }
}
