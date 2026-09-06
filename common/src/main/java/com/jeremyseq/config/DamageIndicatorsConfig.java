package com.jeremyseq.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.jeremyseq.DamageIndicators;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DamageIndicatorsConfig {

    private static final Path CONFIG_PATH = Path.of("config", "jeremyseqsdamageindicators.toml");

    public static DamageIndicatorsConfig INSTANCE = new DamageIndicatorsConfig();

    public boolean enableDirectionalIndicator = true;
    public int directionalIndicatorTexture = 0;
    public int directionalIndicatorDistance = 0;
    public String directionalIndicatorColor = "#CE0025"; // hex RRGGBB
    public int directionalIndicatorAlpha = 255; // 0-255, kept separate from color since fade-out already modifies this
    public boolean enableFadeOut = true;
    public int indicateTime = 25;
    public boolean enableDamageText = true;
    public boolean enableHealthBars = false;
    public boolean onlyShowTargetHealthBar = true;
    public boolean enableBloodOverlay = false;

    private static long lastModifiedTime = 0;

    public Color getDirectionalIndicatorColor() {
        try {
            Color base = Color.decode(directionalIndicatorColor);
            return new Color(base.getRed(), base.getGreen(), base.getBlue(), directionalIndicatorAlpha);
        } catch (NumberFormatException e) {
            DamageIndicators.LOGGER.warn("Invalid directional_indicator.color hex value, using default");
            return new Color(206, 0, 37, directionalIndicatorAlpha);
        }
    }

    public static void load() {
        // Read from whatever currently exists on disk (if anything).
        if (Files.exists(CONFIG_PATH)) {
            CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH).preserveInsertionOrder().build();
            config.load();

            DamageIndicatorsConfig defaults = new DamageIndicatorsConfig();

            INSTANCE.enableDirectionalIndicator = config.getOrElse("directional_indicator.enable", defaults.enableDirectionalIndicator);
            INSTANCE.directionalIndicatorTexture = config.getIntOrElse("directional_indicator.texture", defaults.directionalIndicatorTexture);
            INSTANCE.directionalIndicatorDistance = config.getIntOrElse("directional_indicator.distance", defaults.directionalIndicatorDistance);
            INSTANCE.directionalIndicatorColor = config.getOrElse("directional_indicator.color", defaults.directionalIndicatorColor);
            INSTANCE.directionalIndicatorAlpha = config.getIntOrElse("directional_indicator.alpha", defaults.directionalIndicatorAlpha);
            INSTANCE.enableFadeOut = config.getOrElse("directional_indicator.fade_out", defaults.enableFadeOut);
            INSTANCE.indicateTime = config.getIntOrElse("directional_indicator.indicate_time", defaults.indicateTime);

            INSTANCE.enableDamageText = config.getOrElse("damage_text.enable", defaults.enableDamageText);

            INSTANCE.enableHealthBars = config.getOrElse("health_bar.enable", defaults.enableHealthBars);
            INSTANCE.onlyShowTargetHealthBar = config.getOrElse("health_bar.only_show_target", defaults.onlyShowTargetHealthBar);

            INSTANCE.enableBloodOverlay = config.getOrElse("blood_overlay.enable", defaults.enableBloodOverlay);

            config.close();
        }

        // Always rewrite as a fresh file, so key order always matches writeWithComments() exactly,
        // regardless of what order an old file on disk had.
        save();

        try {
            lastModifiedTime = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
        } catch (IOException ignored) {}
    }

    public static void save() {
        // Delete first so CommentedFileConfig starts from empty rather than merging with old key order.
        try {
            Files.deleteIfExists(CONFIG_PATH);
        } catch (IOException e) {
            DamageIndicators.LOGGER.error("Failed to clear old config before rewrite", e);
        }

        CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH).preserveInsertionOrder().build();
        writeWithComments(config);
        config.close();
    }

    private static void writeWithComments(CommentedFileConfig config) {
        config.setComment("directional_indicator", " arrow that shows where incoming damage came from");
        config.set("directional_indicator.enable", INSTANCE.enableDirectionalIndicator);
        config.setComment("directional_indicator.enable", " enables the directional damage indicator");

        config.set("directional_indicator.texture", INSTANCE.directionalIndicatorTexture);
        config.setComment("directional_indicator.texture", " which indicator texture to use (0 or 1)");

        config.set("directional_indicator.distance", INSTANCE.directionalIndicatorDistance);
        config.setComment("directional_indicator.distance", " distance from the center of the screen that the indicator appears");

        config.set("directional_indicator.color", INSTANCE.directionalIndicatorColor);
        config.setComment("directional_indicator.color", " hex color code, e.g. #CE0025");

        config.set("directional_indicator.alpha", INSTANCE.directionalIndicatorAlpha);
        config.setComment("directional_indicator.alpha", " opacity, 0-255");

        config.set("directional_indicator.fade_out", INSTANCE.enableFadeOut);
        config.setComment("directional_indicator.fade_out", " fade out the damage indicator over time");

        config.set("directional_indicator.indicate_time", INSTANCE.indicateTime);
        config.setComment("directional_indicator.indicate_time", " time for damage indicator to show (in ticks)");

        config.setComment("damage_text", " floating text that shows the amount of damage done to an entity");
        config.set("damage_text.enable", INSTANCE.enableDamageText);
        config.setComment("damage_text.enable", " enables damage text");

        config.setComment("health_bar", " a text health bar for entities");
        config.set("health_bar.enable", INSTANCE.enableHealthBars);
        config.setComment("health_bar.enable", " enables health bar");
        config.set("health_bar.only_show_target", INSTANCE.onlyShowTargetHealthBar);
        config.setComment("health_bar.only_show_target", " only show health bar for entities the player is directly looking at (if false, shows health bar for all nearby entities)");

        config.setComment("blood_overlay", " an overlay of red outlining your screen when you take damage");
        config.set("blood_overlay.enable", INSTANCE.enableBloodOverlay);
        config.setComment("blood_overlay.enable", " enables blood overlay");

        config.save();
    }

    public static void reloadIfChanged() {
        try {
            long currentModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            if (currentModified != lastModifiedTime) {
                load();
                DamageIndicators.LOGGER.info("JeremySeq's Damage Indicators config reloaded");
            }
        } catch (IOException ignored) {}
    }
}