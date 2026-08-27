package com.jeremyseq.fabric;

import com.jeremyseq.damage_text.DamageTextRenderer;
import com.jeremyseq.damage_text.HealthBarRenderer;
import com.jeremyseq.overlays.IndicatorOverlay;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;

public class FabricRenderEvents {
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            DamageTextRenderer.render(context.matrixStack(), mc.renderBuffers().bufferSource());
            HealthBarRenderer.render(context.matrixStack());
        });

        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            IndicatorOverlay.render(guiGraphics, mc.getDeltaFrameTime(), mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        });
    }
}
