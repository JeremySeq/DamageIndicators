package com.jeremyseq.damage_text;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DamageTextRenderer {

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Font fontRenderer = mc.font;

        long currentTime = System.currentTimeMillis();
        List<DamageTextHandler.DamageText> damageTexts = new ArrayList<>(DamageTextHandler.getDamageTexts());
        damageTexts.removeIf(text -> currentTime - text.getTimestamp() > 1000);

        for (DamageTextHandler.DamageText text : damageTexts) {
            if (text == null) continue;
            if (!hasLineOfSight(mc.player, mc.player.level(), (float) text.getX(), (float) text.getY(), (float) text.getZ())) {
                continue;
            }

            long age = currentTime - text.getTimestamp();
            float progress = age / (float) text.getDuration();

            float alpha = 1.0f - progress;
            float scale = 1.0f + 0.5f * (1.0f - progress);

            poseStack.pushPose();
            poseStack.translate(
                    text.getX() - renderManager.camera.getPosition().x,
                    text.getY() - renderManager.camera.getPosition().y,
                    text.getZ() - renderManager.camera.getPosition().z
            );
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-0.025F * scale, -0.025F * scale, 0.025F * scale);
            poseStack.translate(0, -progress * 25, 0);

            Font.DisplayMode displayMode = Font.DisplayMode.SEE_THROUGH;
            String damage = String.format("%.0f", text.getDamage());
            int color = getDamageColor(text.getDamage(), alpha).getRGB();
            int textWidth = fontRenderer.width(damage);
            int textHeight = fontRenderer.lineHeight;

            if (getDamageColor(text.getDamage(), alpha).getAlpha() > 25) {
                fontRenderer.drawInBatch(damage, -textWidth / 2f, -textHeight / 2f, color, false,
                        poseStack.last().pose(), bufferSource, displayMode, 0, 15728880);
            }

            poseStack.popPose();
        }
    }

    private static boolean hasLineOfSight(LivingEntity player, Level level, float x, float y, float z) {
        Vec3 vec3 = new Vec3(x, y, z);
        Vec3 vec31 = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        if (vec31.distanceTo(vec3) > 128.0D) {
            return false;
        }
        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                .getType() == HitResult.Type.MISS;
    }

    private static Color getDamageColor(float damage, float alpha) {
        if (damage > 15) return new Color(0, 0, 0, alpha);
        if (damage > 10) return new Color(1f, 34 / 255f, 34 / 255f, alpha);
        if (damage > 7) return new Color(1f, 130 / 255f, 0f, alpha);
        return new Color(1f, 240 / 255f, 0f, alpha);
    }
}
