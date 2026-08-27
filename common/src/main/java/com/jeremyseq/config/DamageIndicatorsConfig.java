package com.jeremyseq.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class DamageIndicatorsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "jeremyseqsdamageindicators.json");

    public static DamageIndicatorsConfig INSTANCE = new DamageIndicatorsConfig();

    public boolean enableDirectionalIndicator = true;
    public int directionalIndicatorTexture = 0;
    public int directionalIndicatorDistance = 0;
    public int[] directionalIndicatorColor = {206, 0, 37, 255};
    public boolean enableFadeOut = true;
    public int indicateTime = 25;
    public boolean enableDamageText = true;
    public boolean enableHealthBars = false;
    public boolean onlyShowTargetHealthBar = true;
    public boolean enableBloodOverlay = false;

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, DamageIndicatorsConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}