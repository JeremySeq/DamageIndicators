package dev.jeremyseq.damage_text;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.jeremyseq.config.DamageIndicatorsConfig;
import dev.jeremyseq.events.DamageHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class HealthBarRenderer {

    private static final int FULL_BRIGHT = 0xF000F0;

    public static void render(SubmitNodeCollector collector, Camera camera) {
        if (!DamageIndicatorsConfig.INSTANCE.enableHealthBars) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (DamageIndicatorsConfig.INSTANCE.onlyShowTargetHealthBar) {
            if (mc.hitResult instanceof EntityHitResult entityHitResult) {
                int id = entityHitResult.getEntity().getId();
                renderHealthBar(collector, id, camera);
            }
        } else {
            for (int entityId : new ArrayList<>(DamageHandler.lastHealthForOtherEntities.keySet())) {
                renderHealthBar(collector, entityId, camera);
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

    private static void renderHealthBar(SubmitNodeCollector collector, int entityId, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (!(mc.level.getEntity(entityId) instanceof LivingEntity entity)) {
            return;
        }
        Float health = DamageHandler.lastHealthForOtherEntities.get(entityId);
        if (health == null) {
            return;
        }

        float y = (float) (entity.getY() + entity.getBbHeight() + .4f);

        if (!hasLineOfSight(mc.player, mc.player.level(), (float) entity.getX(), y, (float) entity.getZ(), 20d)) {
            return;
        }

        Font font = mc.font;
        Vec3 camPos = camera.position();

        PoseStack poseStack = new PoseStack();

        // translate to just above the entity
        poseStack.translate(
                (float) (entity.getX() - camPos.x),
                (float) (y - camPos.y),
                (float) (entity.getZ() - camPos.z));

        // face the player
        poseStack.rotate(camera.rotation());
        poseStack.rotate(Axis.YP.rotationDegrees(180f));
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        String current = String.valueOf(Math.round(health));
        String maxHealth = String.valueOf((int) Math.ceil(entity.getMaxHealth()));
        String text = current + " / " + maxHealth;

        float textWidth = font.width(text);
        float textHeight = font.lineHeight;

        collector.submitText(
                poseStack,
                -textWidth / 2f,
                -textHeight / 2f,
                Component.literal(text).getVisualOrderText(),
                false,
                Font.DisplayMode.SEE_THROUGH,
                FULL_BRIGHT,
                0xFFFFFFFF,
                0,
                0
        );
    }

}
