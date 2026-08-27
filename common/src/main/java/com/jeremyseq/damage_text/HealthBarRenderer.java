package com.jeremyseq.damage_text;

import com.jeremyseq.config.DamageIndicatorsConfig;
import com.jeremyseq.events.DamageHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class HealthBarRenderer {

    public static void render(PoseStack poseStack) {
        if (!DamageIndicatorsConfig.INSTANCE.enableHealthBars) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        if (DamageIndicatorsConfig.INSTANCE.onlyShowTargetHealthBar) {
            if (mc.hitResult instanceof EntityHitResult entityHitResult) {
                int id = entityHitResult.getEntity().getId();
                renderHealthBar(poseStack, id, bufferSource);
            }
        } else {
            for (int entityId : DamageHandler.lastHealthForOtherEntities.keySet()) {
                renderHealthBar(poseStack, entityId, bufferSource);
            }
        }
    }

    private static boolean hasLineOfSight(LivingEntity player, Level level, float x, float y, float z, double distance) {
        Vec3 vec3 = new Vec3(x, y, z);
        Vec3 vec31 = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        if (vec31.distanceTo(vec3) > distance) {
            return false;
        } else {
            return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player)).getType() == HitResult.Type.MISS;
        }
    }

    private static void renderHealthBar(PoseStack poseStack, int entityId, MultiBufferSource.BufferSource bufferSource) {
        assert Minecraft.getInstance().level != null;
        if (!DamageHandler.lastHealthForOtherEntities.containsKey(entityId)) {
            return;
        }
        if (!(Minecraft.getInstance().level.getEntity(entityId) instanceof LivingEntity entity)) {
            return;
        }
        if (!DamageHandler.lastHealthForOtherEntities.containsKey(entityId)) {
            return;
        }

        float y = (float) (entity.getY()+entity.getBbHeight()+.4f);

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Font fontRenderer = mc.font;

        assert mc.player != null;
        if (!hasLineOfSight(mc.player, mc.player.level(), (float) entity.getX(),
                y, (float) entity.getZ(), 20d)) {
            return;
        }

        poseStack.pushPose();

        // Translate to the position of the damage text
        poseStack.translate(entity.getX() - renderManager.camera.getPosition().x, y - renderManager.camera.getPosition().y, entity.getZ() - renderManager.camera.getPosition().z);

        // Apply transformations for better readability
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Font.DisplayMode displayMode = Font.DisplayMode.SEE_THROUGH;
        String damage = String.valueOf(Math.round(DamageHandler.lastHealthForOtherEntities.get(entityId)));
        String maxHealth = String.valueOf((int) Math.ceil(entity.getMaxHealth()));

        String text = damage + " / " + maxHealth;

        // handle color
        int color = new Color(255, 255, 255).getRGB();

        int textWidth = fontRenderer.width(text);
        int textHeight = fontRenderer.lineHeight;

        // render the text
        fontRenderer.drawInBatch(text, -textWidth/2f, -textHeight/2f, color, false, poseStack.last().pose(), bufferSource, displayMode, 0, 15728880);

        poseStack.popPose();
    }

}
