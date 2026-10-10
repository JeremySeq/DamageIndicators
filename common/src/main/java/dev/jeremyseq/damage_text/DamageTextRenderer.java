package dev.jeremyseq.damage_text;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DamageTextRenderer {

    public static void render(MultiBufferSource.BufferSource bufferSource, Camera camera) {
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
            Matrix4f matrix = new Matrix4f();
            if (text == null) {
                continue;
            }
            if (!hasLineOfSight(mc.player, mc.player.level(), (float) text.getX(), (float) text.getY(), (float) text.getZ())) {
                continue;
            }

            long age = currentTime - text.getTimestamp();
            float progress = age / (float) text.getDuration();

            // calculate fade and scale
            float alpha = 1.0f - progress;
            float scale = 1.0f + 0.5f * (1.0f - progress);

            // translate to the position of the damage text
            matrix.translate((float) (text.getX() - renderManager.camera.getPosition().x),
                    (float) (text.getY() - renderManager.camera.getPosition().y),
                    (float) (text.getZ() - renderManager.camera.getPosition().z));


            // rotate damage text to face the player
            matrix.rotate(camera.rotation());
            matrix.rotateY((float) Math.PI);

            // scale stuff
            matrix.scale(-0.025F * scale, -0.025F * scale, 0.025F * scale);

            // move the text up over time
            matrix.translate(0, -progress*25, 0);

            Font.DisplayMode displayMode = Font.DisplayMode.SEE_THROUGH;
            String damage = String.format("%.0f", text.getDamage());

            // handle color
            int color = getDamageColor(text.getDamage(), alpha).getRGB();

            int textWidth = fontRenderer.width(damage);
            int textHeight = fontRenderer.lineHeight;

            // render the text
            if (getDamageColor(text.getDamage(), alpha).getAlpha() > 25) {
                fontRenderer.drawInBatch(damage, -textWidth/2f, -textHeight/2f, color, false, matrix, bufferSource, displayMode, 0, 15728880);
            }
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
