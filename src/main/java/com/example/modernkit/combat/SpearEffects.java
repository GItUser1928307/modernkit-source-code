package com.example.modernkit.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/**
 * Centralized sound and particle effects for ModernKit spears.
 *
 * All effects are triggered by the authoritative server.
 */
public final class SpearEffects {

    private SpearEffects() {
    }

    private static ServerWorld getWorld(ServerPlayerEntity player) {
        return (ServerWorld) player.world;
    }

    private static void playSound(
            ServerPlayerEntity player,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        player.world.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundCategory.PLAYERS,
                volume,
                pitch
        );
    }

    /**
     * Effects when a player performs a spear jab.
     */
    public static void playJabStart(
            ServerPlayerEntity player,
            Vec3d origin,
            Vec3d direction
    ) {

        playSound(
                player,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                0.65F,
                1.15F
        );

        ServerWorld world = getWorld(player);

        // A small particle trail following the direction of the jab.
        for (int i = 1; i <= 5; i++) {

            Vec3d position = origin.add(
                    direction.multiply(i * 0.65D)
            );

            world.spawnParticles(
                    ParticleTypes.CRIT,
                    position.x,
                    position.y,
                    position.z,
                    1,
                    0.025D,
                    0.025D,
                    0.025D,
                    0.01D
            );
        }
    }

    /**
     * Impact effects when a jab successfully damages a target.
     */
    public static void playJabImpact(
            ServerPlayerEntity player,
            LivingEntity target
    ) {

        double x = target.getX();
        double y = target.getY() + target.getHeight() * 0.5D;
        double z = target.getZ();

        player.world.playSound(
                null,
                x,
                y,
                z,
                SoundEvents.ITEM_TRIDENT_HIT,
                SoundCategory.PLAYERS,
                0.75F,
                1.1F
        );

        ServerWorld world = getWorld(player);

        world.spawnParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                x,
                y,
                z,
                5,
                0.20D,
                0.25D,
                0.20D,
                0.05D
        );

        world.spawnParticles(
                ParticleTypes.CRIT,
                x,
                y,
                z,
                8,
                0.25D,
                0.25D,
                0.25D,
                0.08D
        );
    }

    /**
     * Feedback when entering a new charge stage.
     */
    public static void playChargeStage(
            ServerPlayerEntity player,
            SpearChargeState state
    ) {

        SoundEvent sound;
        float volume;
        float pitch;
        int particleCount;

        switch (state) {

            case ENGAGED:
                sound = SoundEvents.ITEM_TRIDENT_THROW;
                volume = 0.40F;
                pitch = 0.85F;
                particleCount = 6;
                break;

            case TIRED:
                sound = SoundEvents.ENTITY_PLAYER_ATTACK_WEAK;
                volume = 0.35F;
                pitch = 0.85F;
                particleCount = 4;
                break;

            case DISENGAGED:
                sound = SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE;
                volume = 0.30F;
                pitch = 0.75F;
                particleCount = 3;
                break;

            default:
                return;
        }

        playSound(player, sound, volume, pitch);

        ServerWorld world = getWorld(player);

        double x = player.getX();
        double y = player.getY() + 1.0D;
        double z = player.getZ();

        world.spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                particleCount,
                0.25D,
                0.25D,
                0.25D,
                0.015D
        );

        if (state == SpearChargeState.ENGAGED) {
            world.spawnParticles(
                    ParticleTypes.CRIT,
                    x,
                    y,
                    z,
                    4,
                    0.20D,
                    0.20D,
                    0.20D,
                    0.04D
            );
        }
    }

    /**
     * Impact effects from a successful charge attack.
     */
    public static void playChargeImpact(
            ServerPlayerEntity player,
            LivingEntity target,
            SpearChargeState state
    ) {

        double x = target.getX();
        double y = target.getY() + target.getHeight() * 0.5D;
        double z = target.getZ();

        float volume =
                state == SpearChargeState.ENGAGED
                        ? 0.95F
                        : 0.70F;

        float pitch =
                state == SpearChargeState.ENGAGED
                        ? 0.85F
                        : 1.0F;

        player.world.playSound(
                null,
                x,
                y,
                z,
                SoundEvents.ITEM_TRIDENT_HIT,
                SoundCategory.PLAYERS,
                volume,
                pitch
        );

        ServerWorld world = getWorld(player);

        int critCount =
                state == SpearChargeState.ENGAGED
                        ? 12
                        : 6;

        world.spawnParticles(
                ParticleTypes.CRIT,
                x,
                y,
                z,
                critCount,
                0.30D,
                0.30D,
                0.30D,
                0.12D
        );

        world.spawnParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                x,
                y,
                z,
                6,
                0.25D,
                0.25D,
                0.25D,
                0.06D
        );

        if (state == SpearChargeState.DISENGAGED) {
            world.spawnParticles(
                    ParticleTypes.CLOUD,
                    x,
                    y,
                    z,
                    4,
                    0.20D,
                    0.15D,
                    0.20D,
                    0.04D
            );
        }
    }
}
