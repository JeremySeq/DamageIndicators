package dev.jeremyseq.damage_text;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DamageTextRenderer {

    private static final int FULL_BRIGHT = 0xF000F0;

    public static void render(SubmitNodeCollector collector, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        Font font = mc.font;
        Vec3 camPos = camera.position();

        long currentTime = System.currentTimeMillis();
        List<DamageTextHandler.DamageText> damageTexts = new ArrayList<>(DamageTextHandler.getDamageTexts());

        for (DamageTextHandler.DamageText text : damageTexts) {
            if (text == null) {
                continue;
            }

            long age = currentTime - text.getTimestamp();
            if (age > 1000) {
                continue;
            }

            if (!hasLineOfSight(mc.player, mc.level, (float) text.getX(), (float) text.getY(), (float) text.getZ())) {
                continue;
            }

            float progress = age / (float) text.getDuration();

            // calculate fade and scale
            float alpha = 1.0f - progress;
            float scale = 1.0f + 0.5f * (1.0f - progress);

            int alpha8 = Math.round(alpha * 255f);
            if (alpha8 <= 25) {
                continue;
            }

            PoseStack poseStack = new PoseStack();

            // translate to the position of the damage text
            poseStack.translate((float) (text.getX() - camPos.x), (float) (text.getY() - camPos.y), (float) (text.getZ() - camPos.z));

            // rotate damage text to face the player
            poseStack.rotate(camera.rotation());
            poseStack.rotate(Axis.YP.rotation((float) Math.PI));

            // scale stuff
            poseStack.scale(-0.025F * scale, -0.025F * scale, 0.025F * scale);

            // move the text up over time
            poseStack.translate(0, -progress*25, 0);

            String damage = String.format("%.0f", text.getDamage());

            // handle color
            int color = getDamageColor(text.getDamage(), alpha8/255f).getRGB();

            float textWidth = font.width(damage);
            float textHeight = font.lineHeight;

            // render the text
            collector.submitText(
                    poseStack,
                    -textWidth / 2f,
                    -textHeight / 2f,
                    Component.literal(damage).getVisualOrderText(),
                    false,
                    Font.DisplayMode.SEE_THROUGH,
                    FULL_BRIGHT,
                    color,
                    0,
                    0
            );
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