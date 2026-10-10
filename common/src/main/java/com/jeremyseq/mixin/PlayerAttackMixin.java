package com.jeremyseq.mixin;

import com.jeremyseq.CritTracker;
import com.jeremyseq.DamageIndicators;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void checkCrit(Entity target, CallbackInfo ci) {
        DamageIndicators.LOGGER.info("attack inject");
        @SuppressWarnings("DataFlowIssue") Player self = (Player) (Object) this;
        float strength = self.getAttackStrengthScale(0.5F);

        CritTracker.lastAttackWasCrit = strength > 0.9f
                && self.fallDistance > 0.0F
                && !self.onGround()
                && !self.onClimbable()
                && !self.isInWater()
                && !self.hasEffect(MobEffects.BLINDNESS)
                && !self.isPassenger();
    }
}