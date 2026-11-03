package com.example.modernkit.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import java.util.List;

public final class SmashAttack {
    public static final float MIN_FALL = 1.5F;
    public static final double RADIUS = 3.5D;
    public static final float KNOCKBACK_SCALE = 0.3F;

    private SmashAttack() {}

    public static boolean canSmash(PlayerEntity player) {
        return player.getMainHandStack().getItem() == ModItems.MACE
                && player.fallDistance > MIN_FALL
                && !player.isFallFlying();
    }

    public static float bonusDamage(float fall) {
        if (fall <= 3.0F) return 4.0F * fall;
        if (fall <= 8.0F) return 12.0F + 2.0F * (fall - 3.0F);
        return 22.0F + (fall - 8.0F);
    }

    public static void doEffects(PlayerEntity player, Entity target, float fall) {
        if (player.world.isClient) return;
        ServerWorld sw = (ServerWorld) player.world;
        double y = target.getBodyY(0.5D);

        // Particles - 1.16.5 doesn't have gust/wind burst, so we fake it
        sw.spawnParticles(ParticleTypes.EXPLOSION, target.getX(), y, target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        sw.spawnParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.2D, target.getZ(), 12, 0.6D, 0.2D, 0.6D, 0.05D);
        sw.spawnParticles(ParticleTypes.CRIT, target.getX(), y, target.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.2D);
        if (fall > 5.0F) {
            sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), y, target.getZ(), 2, 0.6D, 0.2D, 0.6D, 0.0D);
        }

        // Sounds - closest vanilla equivalents to the real mace sounds
        sw.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1.0F, 0.6F);
        sw.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.7F, 1.2F);
        if (fall > 5.0F) {
            sw.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, 0.8F, 0.7F);
        }
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
