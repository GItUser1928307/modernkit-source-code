package com.example.modernkit.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

public final class SmashAttack {
    // Tunable values
    public static final float MIN_FALL = 1.5F;
    public static final double RADIUS = 3.5D;
    public static final float KNOCKBACK_SCALE = 0.3F;

    private SmashAttack() {}

    public static boolean canSmash(PlayerEntity player) {
        return player.getMainHandStack().getItem() == ModItems.MACE
                && player.fallDistance > MIN_FALL
                && !player.isFallFlying();
    }

    // +4 per block for the first 3, +2 for the next 5, +1 beyond that
    public static float bonusDamage(float fall) {
        if (fall <= 3.0F) return 4.0F * fall;
        if (fall <= 8.0F) return 12.0F + 2.0F * (fall - 3.0F);
        return 22.0F + (fall - 8.0F);
    }

    public static void knockbackNearby(PlayerEntity player, Entity target, float fall) {
        Box box = target.getBoundingBox().expand(RADIUS);
        List<LivingEntity> list = player.world.getEntitiesByClass(LivingEntity.class, box, e ->
                e != player
                        && e != target
                        && !player.isTeammate(e)
                        && !(e instanceof ArmorStandEntity && ((ArmorStandEntity) e).isMarker())
                        && !(e instanceof TameableEntity && ((TameableEntity) e).isOwner(player))
                        && target.squaredDistanceTo(e) <= RADIUS * RADIUS);

        for (LivingEntity e : list) {
            double dx = e.getX() - target.getX();
            double dz = e.getZ() - target.getZ();
            double dist = Math.sqrt(target.squaredDistanceTo(e));
            float strength = (float) ((RADIUS - dist) * KNOCKBACK_SCALE);
            if (fall > 5.0F) strength *= 2.0F;
            if (strength <= 0.0F) continue;

            e.takeKnockback(strength, -dx, -dz);
            e.velocityModified = true;
            if (e instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) e).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(e));
            }
        }
    }
}
