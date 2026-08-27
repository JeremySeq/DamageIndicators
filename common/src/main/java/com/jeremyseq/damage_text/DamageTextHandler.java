package com.jeremyseq.damage_text;


import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;
import java.util.List;

public class DamageTextHandler {
    public static final boolean SELF_DAMAGE_TEXT = false;
    public static final List<DamageText> damageTexts = new ArrayList<>();

    public static List<DamageText> getDamageTexts() {
        return damageTexts;
    }

    public static class DamageText {
        private final double x;
        private final double y;
        private final double z;
        private final float damage;
        private final DamageSource source;
        private final long timestamp;
        private final long duration = 1000; // 1 second duration

        public DamageText(double x, double y, double z, float damage, DamageSource source) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.damage = damage;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public float getDamage() {
            return damage;
        }

        public DamageSource getSource() {
            return source;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getDuration() {
            return duration;
        }
    }
}
