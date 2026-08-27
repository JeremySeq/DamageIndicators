package com.jeremyseq.forge;

import com.jeremyseq.DamageIndicators;
import com.jeremyseq.damage_text.DamageTextRenderer;
import com.jeremyseq.damage_text.HealthBarRenderer;
import com.jeremyseq.overlays.IndicatorOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DamageIndicators.MOD_ID, value = Dist.CLIENT)
public class ForgeRenderEvents {

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
    }
}