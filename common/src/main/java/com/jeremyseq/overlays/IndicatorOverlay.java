package com.jeremyseq.overlays;

import com.jeremyseq.DamageIndicators;
import com.jeremyseq.config.DamageIndicatorsConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.*;

public class IndicatorOverlay {

    private static ResourceLocation INDICATOR_TEXTURE = null; // set after config is loaded

    private static final ResourceLocation BLOOD_OVERLAY = new ResourceLocation(DamageIndicators.MOD_ID,
            "textures/overlays/blood_overlay.png");

    public static Vec3 damageSource = null;

    public static boolean showDirectional = false;
    public static float counter = 0;

    // Hit marker state
    private static boolean showHitmarker = false;
    private static float hitmarkerCounter = 0f;

    // Blood overlay state
    private static boolean showBlood = false;
    private static float bloodAlpha = 0f;
    private static final float BLOOD_FADE_SPEED = 0.02f;

    public static void setup() {
        INDICATOR_TEXTURE = new ResourceLocation(DamageIndicators.MOD_ID,
                "textures/overlays/directional_indicator_" + DamageIndicatorsConfig.INSTANCE.directionalIndicatorTexture + ".png");
    }

    public static void triggerOverlay(Vec3 sourcePosition) {
        damageSource = sourcePosition;
        showDirectional = true;
        counter = 0;

        // Trigger blood effect
        showBlood = true;
        bloodAlpha = 1f; // Fully opaque
    }

    public static void triggerHitmarker() {
        showHitmarker = true;
        hitmarkerCounter = 0f;
    }

    public static void render(GuiGraphics poseStack, float partialTick, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {

            int x = width / 2;
            int y = height / 2;

            float deltaFrameTime = Minecraft.getInstance().getDeltaFrameTime();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            if (showHitmarker) {
                hitmarkerCounter += deltaFrameTime;

                if (hitmarkerCounter >= DamageIndicatorsConfig.INSTANCE.hitmarkerDuration) {
                    showHitmarker = false;
                } else {
                    if (DamageIndicatorsConfig.INSTANCE.enableHitmarker) {
                        float alpha = 1f - (hitmarkerCounter / DamageIndicatorsConfig.INSTANCE.hitmarkerDuration);
                        int alphaInt = Math.max(0, Math.min(255, (int) (alpha * 255f)));
                        poseStack.flush(); // just in case ig
                        drawX(poseStack, x, y,
                                DamageIndicatorsConfig.INSTANCE.hitmarkerSize,
                                DamageIndicatorsConfig.INSTANCE.hitmarkerThickness,
                                DamageIndicatorsConfig.INSTANCE.hitmarkerGap,
                                new Color(255, 255, 255, alphaInt)
                        );
                        poseStack.flush();
                    }
                }
            }

            // blood overlay
            if (showBlood && DamageIndicatorsConfig.INSTANCE.enableBloodOverlay) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, BLOOD_OVERLAY);

                // Set color with alpha fading over time
                RenderSystem.setShaderColor(1f, 1f, 1f, bloodAlpha);
                bloodAlpha -= BLOOD_FADE_SPEED * deltaFrameTime;

                if (bloodAlpha <= 0f) {
                    showBlood = false;
                    bloodAlpha = 0f;
                }

                poseStack.blit(BLOOD_OVERLAY, 0, 0, width, height, 0, 0, width, height, width, height);

                RenderSystem.setShaderColor(1, 1, 1, 1);
            }

            // directional indicator
            if (showDirectional && DamageIndicatorsConfig.INSTANCE.enableDirectionalIndicator) {

                counter += deltaFrameTime;

                if (counter >= DamageIndicatorsConfig.INSTANCE.indicateTime) {
                    showDirectional = false;
                }

                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                float r = DamageIndicatorsConfig.INSTANCE.getDirectionalIndicatorColor().getRed() / 255f;
                float g = DamageIndicatorsConfig.INSTANCE.getDirectionalIndicatorColor().getGreen() / 255f;
                float b = DamageIndicatorsConfig.INSTANCE.getDirectionalIndicatorColor().getBlue() / 255f;
                float a = DamageIndicatorsConfig.INSTANCE.getDirectionalIndicatorColor().getAlpha() / 255f;

                // fade out
                if (DamageIndicatorsConfig.INSTANCE.enableFadeOut) {
                    a = a * (DamageIndicatorsConfig.INSTANCE.indicateTime - counter) / DamageIndicatorsConfig.INSTANCE.indicateTime;
                }

                RenderSystem.setShaderColor(r, g, b, a);

                assert Minecraft.getInstance().player != null;
                double finalAngle = calculateFinalAngle(Minecraft.getInstance().player.getForward(),
                        Minecraft.getInstance().player.position(), Minecraft.getInstance().player.position().add(damageSource.subtract(Minecraft.getInstance().player.position())));

                // Convert the angle to radians
                float radians = (float) Math.toRadians(finalAngle);
                float radians2 = (float) Math.toRadians(finalAngle - 90);

                int textureWidth = 100;

                int distanceFromCenter = DamageIndicatorsConfig.INSTANCE.directionalIndicatorDistance;

                // Calculate the position based on distance from center
                float indicatorX = x + (float) (distanceFromCenter * Math.cos(radians2));
                float indicatorY = y + (float) (distanceFromCenter * Math.sin(radians2));

                // Apply translation to move the center of the image to the origin
                poseStack.pose().translate(indicatorX, indicatorY, 0);

                Quaternionf quaternion = new Quaternionf(0, 0, Math.sin(radians / 2), Math.cos(radians / 2));

                // Apply rotation around the origin
                poseStack.pose().mulPose(quaternion);

                // Apply translation to move the image back to its original position
                poseStack.pose().translate(-indicatorX, -indicatorY, 0);

                // Draw the radar image
                poseStack.blit(INDICATOR_TEXTURE, (int) (indicatorX - textureWidth / 2f), (int) (indicatorY - textureWidth / 2f), 0, 0, textureWidth, textureWidth, textureWidth, textureWidth);

                // Restore the original transformation matrix
                poseStack.pose().translate(indicatorX, indicatorY, 0);
                poseStack.pose().mulPose(quaternion.invert());
                poseStack.pose().translate(-indicatorX, -indicatorY, 0);

                RenderSystem.setShaderColor(1, 1, 1, 1);
            } else {
                counter = 0;
            }
        }
    }

    private static double calculateFinalAngle(Vec3 playerForward, Vec3 playerPosition, Vec3 damagePosition) {
        double playerAngle = Math.atan2(playerForward.x, playerForward.z) * 180 / Math.PI;

        Vec2 enemyVec = new Vec2((float) damagePosition.x, (float) damagePosition.z).add(new Vec2((float) playerPosition.x, (float) playerPosition.z).negated());

        double enemyAngle = Math.atan2(enemyVec.x, enemyVec.y) * 180 / Math.PI;

        double finalAngle = enemyAngle - playerAngle;

        return -finalAngle;
    }

    private static void drawX(GuiGraphics guiGraphics, int centerX, int centerY, int size, float thickness, float gap, Color color) {
        drawRotatedBar(guiGraphics, centerX, centerY, size, thickness, gap, 45f, color);
        drawRotatedBar(guiGraphics, centerX, centerY, size, thickness, gap, -45f, color);
    }

    private static void drawRotatedBar(GuiGraphics guiGraphics, int centerX, int centerY, int halfLength, float thickness, float gap, float angleDegrees, Color color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().mulPose(new Quaternionf().rotateZ((float) Math.toRadians(angleDegrees)));

        float halfThickness = thickness / 2f;

        fillFloat(guiGraphics, gap, -halfThickness, halfLength, halfThickness, color);
        fillFloat(guiGraphics, -halfLength, -halfThickness, -gap, halfThickness, color);

        guiGraphics.pose().popPose();
    }

    private static void fillFloat(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, Color color) {
        if (x1 > x2) {
            float tmp = x1; x1 = x2; x2 = tmp;
        }
        if (y1 > y2) {
            float tmp = y1; y1 = y2; y2 = tmp;
        }

        Matrix4f matrix = guiGraphics.pose().last().pose();
        float a = color.getAlpha() / 255f;
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
}
