package dev.jeremyseq.forge;

import dev.jeremyseq.DamageIndicators;
import dev.jeremyseq.damage_text.DamageTextRenderer;
import dev.jeremyseq.damage_text.HealthBarRenderer;
import dev.jeremyseq.overlays.DirectionalIndicatorImages;
import dev.jeremyseq.overlays.IndicatorOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = DamageIndicators.MOD_ID, value = Dist.CLIENT)
public class ForgeClientEvents {

    @SubscribeEvent
    public static void onRenderWorldStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft mc = Minecraft.getInstance();
            DamageTextRenderer.render(event.getPoseStack(), mc.renderBuffers().bufferSource());
            HealthBarRenderer.render(event.getPoseStack());
        }
    }

    @Mod.EventBusSubscriber(modid = DamageIndicators.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEventBus {
        @SubscribeEvent
        public static void registerOverlay(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("damage_indicator", (gui, guiGraphics, partialTick, width, height) -> {
                IndicatorOverlay.render(guiGraphics, gui.getMinecraft().getDeltaFrameTime(), width, height);
            });
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