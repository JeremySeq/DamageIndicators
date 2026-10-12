package dev.jeremyseq.overlays;

import dev.jeremyseq.DamageIndicators;
import dev.jeremyseq.config.DamageIndicatorsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;

public class IndicatorOverlay {

    private static final Identifier BLOOD_OVERLAY = Identifier.fromNamespaceAndPath(DamageIndicators.MOD_ID, "textures/overlays/blood_overlay.png");

    private static final int SUBPIXEL = 8;

    public static Vec3 damageSource = null;

    public static boolean showDirectional = false;
    public static float counter = 0;

    // Hit marker state
    private static boolean showHitmarker = false;
    private static float hitmarkerCounter = 0f;
    private static boolean wasKill = false;
    private static boolean wasCrit = false;

    // Blood overlay state
    private static boolean showBlood = false;
    private static float bloodAlpha = 0f;
    private static final float BLOOD_FADE_SPEED = 0.02f;

    public static void triggerOverlay(Vec3 sourcePosition) {
        damageSource = sourcePosition;
        showDirectional = true;
        counter = 0;

        // Trigger blood effect
        showBlood = true;
        bloodAlpha = 1f; // Fully opaque
    }

    public static void triggerHitmarker(boolean wasKill, boolean wasCrit) {
        showHitmarker = true;
        hitmarkerCounter = 0f;
        IndicatorOverlay.wasKill = wasKill;
        IndicatorOverlay.wasCrit = wasCrit;
    }

    public static void render(GuiGraphicsExtractor graphics, float deltaTicks, int width, int height) {
        Minecraft mc = Minecraft.getInstance();

        // VERIFY: Options#hideGui was replaced by Hud#isHidden in 26.2. This may also be redundant,
        // since HUD elements are normally not drawn while the HUD is hidden.
        if (mc.gui.hud.isHidden() || mc.options.getCameraType().isMirrored()) {
            return;
        }

        int x = width / 2;
        int y = height / 2;

        // hit marker
        if (showHitmarker) {
            hitmarkerCounter += deltaTicks;

            if (hitmarkerCounter >= DamageIndicatorsConfig.INSTANCE.hitmarkerDuration) {
                showHitmarker = false;
            } else if (DamageIndicatorsConfig.INSTANCE.enableHitmarker) {
                float progress = hitmarkerCounter / DamageIndicatorsConfig.INSTANCE.hitmarkerDuration;
                float alpha = 1f - (progress * progress); // quadratic ease out

                Color baseColor;
                if (wasKill) {
                    baseColor = DamageIndicatorsConfig.INSTANCE.getHitmarkerKillColor();
                } else if (wasCrit) {
                    baseColor = DamageIndicatorsConfig.INSTANCE.getHitmarkerCritColor();
                } else {
                    baseColor = DamageIndicatorsConfig.INSTANCE.getHitmarkerColor();
                }

                drawX(graphics, x, y,
                        DamageIndicatorsConfig.INSTANCE.hitmarkerSize,
                        DamageIndicatorsConfig.INSTANCE.hitmarkerThickness,
                        DamageIndicatorsConfig.INSTANCE.hitmarkerGap,
                        withAlpha(baseColor, alpha)
                );
            }
        }

        // blood overlay
        if (showBlood && DamageIndicatorsConfig.INSTANCE.enableBloodOverlay) {
            int tint = new Color(1f, 1f, 1f, Math.clamp(bloodAlpha, 0f, 1f)).getRGB();

            bloodAlpha -= BLOOD_FADE_SPEED * deltaTicks;
            if (bloodAlpha <= 0f) {
                showBlood = false;
                bloodAlpha = 0f;
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, BLOOD_OVERLAY, 0, 0, 0f, 0f, width, height, width, height, tint);
        }

        // directional indicator
        if (showDirectional && DamageIndicatorsConfig.INSTANCE.enableDirectionalIndicator) {
            counter += deltaTicks;

            if (counter >= DamageIndicatorsConfig.INSTANCE.indicateTime) {
                showDirectional = false;
            }

            if (mc.player == null || damageSource == null) {
                return;
            }

            Color color = DamageIndicatorsConfig.INSTANCE.getDirectionalIndicatorColor();
            float a = color.getAlpha() / 255f;

            // fade out
            if (DamageIndicatorsConfig.INSTANCE.enableFadeOut) {
                a = a * (DamageIndicatorsConfig.INSTANCE.indicateTime - counter) / DamageIndicatorsConfig.INSTANCE.indicateTime;
            }

            int tint = withAlpha(color, a).getRGB();

            double finalAngle = calculateFinalAngle(mc.player.getForward(),
                    mc.player.position(), mc.player.position().add(damageSource.subtract(mc.player.position())));

            // Convert the angle to radians
            float radians = (float) Math.toRadians(finalAngle);
            float radians2 = (float) Math.toRadians(finalAngle - 90);

            int textureWidth = 100;
            int distanceFromCenter = DamageIndicatorsConfig.INSTANCE.directionalIndicatorDistance;

            // Position on a circle around the crosshair
            float indicatorX = x + (float) (distanceFromCenter * Math.cos(radians2));
            float indicatorY = y + (float) (distanceFromCenter * Math.sin(radians2));

            // DirectionalIndicatorImages#get must now return an Identifier (see notes)
            Identifier texture = DirectionalIndicatorImages.get(DamageIndicatorsConfig.INSTANCE.directionalIndicatorTexture);

            if (texture != null) {
                // Rotate around the indicator's center. This replaces the old
                // translate(+i) / mulPose / translate(-i) / blit / inverse-rotate dance:
                // push, move origin to the indicator, rotate, draw centered on the origin, pop.
                graphics.pose().pushMatrix();
                graphics.pose().translate(indicatorX, indicatorY);
                graphics.pose().rotate(radians);

                graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                        -textureWidth / 2, -textureWidth / 2,
                        0f, 0f, textureWidth, textureWidth, textureWidth, textureWidth, tint);

                graphics.pose().popMatrix();
            } else {
                DamageIndicators.LOGGER.error("Invalid directional_indicator.texture: {}", DamageIndicatorsConfig.INSTANCE.directionalIndicatorTexture);
            }
        } else {
            counter = 0;
        }
    }

    private static double calculateFinalAngle(Vec3 playerForward, Vec3 playerPosition, Vec3 damagePosition) {
        double playerAngle = Math.atan2(playerForward.x, playerForward.z) * 180 / Math.PI;

        Vec2 enemyVec = new Vec2((float) damagePosition.x, (float) damagePosition.z).add(new Vec2((float) playerPosition.x, (float) playerPosition.z).negated());

        double enemyAngle = Math.atan2(enemyVec.x, enemyVec.y) * 180 / Math.PI;

        double finalAngle = enemyAngle - playerAngle;

        return -finalAngle;
    }

    private static void drawX(GuiGraphicsExtractor graphics, int centerX, int centerY, int size, float thickness, float gap, Color color) {
        drawRotatedBar(graphics, centerX, centerY, size, thickness, gap, 45f, color);
        drawRotatedBar(graphics, centerX, centerY, size, thickness, gap, -45f, color);
    }

    private static void drawRotatedBar(GuiGraphicsExtractor graphics, int centerX, int centerY, int halfLength, float thickness, float gap, float angleDegrees, Color color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().rotate((float) Math.toRadians(angleDegrees));

        float halfThickness = thickness / 2f;

        fillFloat(graphics, gap, -halfThickness, halfLength, halfThickness, color);
        fillFloat(graphics, -halfLength, -halfThickness, -gap, halfThickness, color);

        graphics.pose().popMatrix();
    }

    /**
     * Float-precision rectangle: fill() takes ints, so draw at larger size, then scale down.
     */
    private static void fillFloat(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, Color color) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(1f / SUBPIXEL, 1f / SUBPIXEL);
        graphics.fill(Math.round(x1 * SUBPIXEL), Math.round(y1 * SUBPIXEL), Math.round(x2 * SUBPIXEL), Math.round(y2 * SUBPIXEL), color.getRGB());
        graphics.pose().popMatrix();
    }

    /** Same RGB as {@code c}, with the given alpha (0..1). */
    private static Color withAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.round(Math.clamp(alpha, 0f, 1f) * 255f));
    }
}