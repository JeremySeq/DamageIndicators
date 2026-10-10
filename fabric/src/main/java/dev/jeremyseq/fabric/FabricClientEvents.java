package dev.jeremyseq.fabric;

import dev.jeremyseq.DamageIndicators;
import dev.jeremyseq.damage_text.DamageTextRenderer;
import dev.jeremyseq.damage_text.HealthBarRenderer;
import dev.jeremyseq.overlays.DirectionalIndicatorImages;
import dev.jeremyseq.overlays.IndicatorOverlay;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricClientEvents {
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

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new IdentifiableResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(DamageIndicators.MOD_ID, "directional_indicator_images");
                    }

                    private final SimplePreparableReloadListener<Void> listener =
                            new SimplePreparableReloadListener<Void>() {
                                @Override
                                protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                                    return null;
                                }

                                @Override
                                protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                                    DirectionalIndicatorImages.reload(resourceManager);
                                }
                            };

                    @Override
                    public @NotNull CompletableFuture<Void> reload(
                            PreparationBarrier synchronizer,
                            ResourceManager manager,
                            ProfilerFiller prepareProfiler,
                            ProfilerFiller applyProfiler,
                            Executor prepareExecutor,
                            Executor applyExecutor
                    ) {
                        return listener.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
                    }
                });
    }
}
