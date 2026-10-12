package dev.jeremyseq.neoforge;

import dev.jeremyseq.DamageIndicators;
import dev.jeremyseq.damage_text.DamageTextRenderer;
import dev.jeremyseq.damage_text.HealthBarRenderer;
import dev.jeremyseq.overlays.DirectionalIndicatorImages;
import dev.jeremyseq.overlays.IndicatorOverlay;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = DamageIndicators.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        DamageTextRenderer.render(event.getSubmitNodeCollector(), camera);
        HealthBarRenderer.render(event.getSubmitNodeCollector(), camera);
    }

    @EventBusSubscriber(modid = DamageIndicators.MOD_ID, value = Dist.CLIENT)
    public static class ModEventBus {
        @SubscribeEvent
        public static void registerOverlay(RegisterGuiLayersEvent event) {
            event.registerAboveAll(
                    Identifier.fromNamespaceAndPath(DamageIndicators.MOD_ID, "damage_indicator"),
                    (guiGraphics, deltaTracker) -> {
                        Window window = Minecraft.getInstance().getWindow();
                        IndicatorOverlay.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks(), window.getGuiScaledWidth(), window.getGuiScaledHeight());
                    }
            );
        }

        @SubscribeEvent
        public static void onAddReloadListeners(AddClientReloadListenersEvent event) {
            ResourceManagerReloadListener listener = DirectionalIndicatorImages::reload;
            event.addListener(
                    Identifier.fromNamespaceAndPath(DamageIndicators.MOD_ID, "directional_indicator_images"),
                    listener);
        }
    }
}