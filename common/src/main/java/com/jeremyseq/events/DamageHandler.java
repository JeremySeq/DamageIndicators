package com.jeremyseq.events;

import com.jeremyseq.config.DamageIndicatorsConfig;
import com.jeremyseq.damage_text.DamageTextHandler;
import com.jeremyseq.overlays.IndicatorOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DamageHandler {

    private static float lastHealth = -1;
    public static final HashMap<Integer, Float> lastHealthForOtherEntities = new HashMap<>();

    public static void register() {
        ClientTickHandler.register(DamageHandler::onClientTick);
    }

    private static void onClientTick() {
        if (Minecraft.getInstance().player == null) return;
        Player player = Minecraft.getInstance().player;

        // directional indicator
        if (DamageIndicatorsConfig.INSTANCE.enableDirectionalIndicator) {
            float currentHealth = player.getHealth();
            boolean tookDamage = lastHealth != -1 && currentHealth < lastHealth;
            lastHealth = currentHealth;

            if (player.getLastDamageSource() != null && player.getLastDamageSource().getSourcePosition() != null) {
                Vec3 damageSource = player.getLastDamageSource().getSourcePosition();
                if (tookDamage) {
                    IndicatorOverlay.triggerOverlay(damageSource);
                }
            }
        }

        // damage text
        if (DamageIndicatorsConfig.INSTANCE.enableDamageText) {
            AABB aabb = new AABB(
                    player.getX() - 50, player.getY() - 50, player.getZ() - 50,
                    player.getX() + 50, player.getY() + 50, player.getZ() + 50
            );

            List<Entity> nearbyEntities = player.level().getEntities(player, aabb);
            Set<Integer> inRangeEntityIds = new HashSet<>();

            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity livingEntity) {
                    inRangeEntityIds.add(livingEntity.getId());
                    if (lastHealthForOtherEntities.containsKey(livingEntity.getId())) {
                        if (livingEntity.getHealth() < lastHealthForOtherEntities.get(livingEntity.getId())) {
                            if (DamageTextHandler.SELF_DAMAGE_TEXT || entity.getId() != Minecraft.getInstance().player.getId()) {
                                double x = entity.getX();
                                double y = entity.getY() + entity.getEyeHeight();
                                double z = entity.getZ();
                                float damage = lastHealthForOtherEntities.get(livingEntity.getId()) - livingEntity.getHealth();
                                DamageSource source = livingEntity.getLastDamageSource();

                                DamageTextHandler.damageTexts.add(new DamageTextHandler.DamageText(x, y, z, damage, source));
                            }
                        }
                    }
                    lastHealthForOtherEntities.put(livingEntity.getId(), livingEntity.getHealth());
                }
            }

            lastHealthForOtherEntities.keySet().removeIf(id -> !inRangeEntityIds.contains(id));
        }
    }
}