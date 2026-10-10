package com.jeremyseq.neoforge;

import com.jeremyseq.DamageIndicators;
import com.jeremyseq.damage_text.DamageTextRenderer;
import com.jeremyseq.damage_text.HealthBarRenderer;
import com.jeremyseq.overlays.DirectionalIndicatorImages;
import com.jeremyseq.overlays.IndicatorOverlay;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = DamageIndicators.MOD_ID, value = Dist.CLIENT)
public class ForgeClientEvents {

    @SubscribeEvent
    public static void onRenderWorldStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft mc = Minecraft.getInstance();
            DamageTextRenderer.render(mc.renderBuffers().bufferSource(), event.getCamera());
            HealthBarRenderer.render(event.getPoseStack(), event.getCamera());
        }
    }

    @EventBusSubscriber(modid = DamageIndicators.MOD_ID, value = Dist.CLIENT)
    public static class ModEventBus {
        @SubscribeEvent
        public static void registerOverlay(RegisterGuiLayersEvent event) {
            event.registerAboveAll(
                    ResourceLocation.fromNamespaceAndPath(DamageIndicators.MOD_ID, "damage_indicator"),
                    (guiGraphics, deltaTracker) -> {
                        Window window = Minecraft.getInstance().getWindow();
                        IndicatorOverlay.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks(), window.getGuiScaledWidth(), window.getGuiScaledHeight());
                    }
            );
        }

        @SubscribeEvent
        public static void onAddReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(
                    new SimplePreparableReloadListener<Void>() {
                        @Override
                        protected @NotNull Void prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                            return null;
                        }

                        @Override
                        protected void apply(@NotNull Void object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                            DirectionalIndicatorImages.reload(resourceManager);
                        }
                    }
            );
        }
    }
}