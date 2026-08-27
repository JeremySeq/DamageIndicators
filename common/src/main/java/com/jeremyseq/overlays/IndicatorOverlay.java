package com.jeremyseq.overlays;

import com.jeremyseq.DamageIndicators;
import com.jeremyseq.config.DamageIndicatorsConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class IndicatorOverlay {

    private static ResourceLocation INDICATOR_TEXTURE = null; // set after config is loaded

    private static final ResourceLocation BLOOD_OVERLAY = new ResourceLocation(DamageIndicators.MOD_ID,
            "textures/overlays/blood_overlay.png");

    public static Vec3 damageSource = null;

    public static boolean showDirectional = false;
    public static float counter = 0;

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

    public static void render(GuiGraphics poseStack, float partialTick, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {

            int x = width / 2;
            int y = height / 2;

            float deltaFrameTime = Minecraft.getInstance().getDeltaFrameTime();

            // blood overlay
            if (showBlood && DamageIndicatorsConfig.INSTANCE.enableBloodOverlay) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
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
            }

            // directional indicator
            if (showDirectional && DamageIndicatorsConfig.INSTANCE.enableDirectionalIndicator) {

                counter += deltaFrameTime;

                if (counter >= DamageIndicatorsConfig.INSTANCE.indicateTime) {
                    showDirectional = false;
                }

                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                float r = DamageIndicatorsConfig.INSTANCE.directionalIndicatorColor[0] / 255f;
                float g = DamageIndicatorsConfig.INSTANCE.directionalIndicatorColor[1] / 255f;
                float b = DamageIndicatorsConfig.INSTANCE.directionalIndicatorColor[2] / 255f;
                float a = DamageIndicatorsConfig.INSTANCE.directionalIndicatorColor[3] / 255f;

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
}
