package dev.jeremyseq.fabric;

import dev.jeremyseq.DamageIndicators;
import dev.jeremyseq.damage_text.DamageTextRenderer;
import dev.jeremyseq.damage_text.HealthBarRenderer;
import dev.jeremyseq.overlays.DirectionalIndicatorImages;
import dev.jeremyseq.overlays.IndicatorOverlay;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class FabricClientEvents {
    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
            DamageTextRenderer.render(context.submitNodeCollector(), camera);
            HealthBarRenderer.render(context.submitNodeCollector(), camera);
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(DamageIndicators.MOD_ID, "indicator_overlay"),
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    IndicatorOverlay.render(
                            graphics,
                            deltaTracker.getGameTimeDeltaTicks(),
                            mc.getWindow().getGuiScaledWidth(),
                            mc.getWindow().getGuiScaledHeight());
                });

        ResourceManagerReloadListener listener = DirectionalIndicatorImages::reload;

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                Identifier.fromNamespaceAndPath(DamageIndicators.MOD_ID, "directional_indicator_images"),
                listener);
    }
}