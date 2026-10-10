package dev.jeremyseq;

import dev.jeremyseq.config.DamageIndicatorsConfig;
import dev.jeremyseq.events.ConfigReloader;
import dev.jeremyseq.events.DamageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DamageIndicators {
    public static final String MOD_ID = "jeremyseqsdamageindicators";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        DamageIndicatorsConfig.load();
        ConfigReloader.register();
        DamageHandler.register();
        // Write common init code here.
    }
}
