package com.example.modernkit.combat;

import com.example.modernkit.item.ModItems;
import com.example.modernkit.item.SpearItem;
import com.example.modernkit.item.SpearMaterial;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * ModernKit spear equipment and combat AI for hostile mobs.
 *
 * Supported:
 * - Zombies
 * - Husks
 * - Piglins
 * - Zombified Piglins
 *
 * All combat calculations run on the logical server.
 */
public final class SpearMobCombat {

    private static final Map<MobEntity, MobState> STATES =
            new WeakHashMap<>();

    private static final double CHARGE_START_RANGE_SQUARED = 64.0D;
    private static final double CHARGE_ABORT_RANGE_SQUARED = 144.0D;
    private static final double ATTACK_RANGE_SQUARED = 12.25D;

    private static final int MINIMUM_CHARGE_TICKS = 8;
    private static final int ATTACK_COOLDOWN_TICKS = 32;
    private static final int NAVIGATION_UPDATE_INTERVAL = 5;

    private SpearMobCombat() {
    }

    // =================================================
    // SUPPORTED MOB TYPES
    // =================================================

    public static boolean isSupportedMob(MobEntity mob) {

        if (mob instanceof PiglinEntity) {
            return true;
        }

        return mob instanceof ZombieEntity
                && !(mob instanceof DrownedEntity)
                && !(mob instanceof ZombieVillagerEntity);
    }

    public static boolean shouldOverrideVanillaAttack(MobEntity mob) {

        return mob != null
                && isSupportedMob(mob)
                && mob.getMainHandStack().getItem() instanceof SpearItem;
    }

    // =================================================
    // SPAWN EQUIPMENT
    // =================================================

    public static void equipZombieFamily(ZombieEntity mob) {

        if (mob.world.isClient || mob.isBaby()) {
            return;
        }

        if (mob instanceof DrownedEntity
                || mob instanceof ZombieVillagerEntity) {
            return;
        }

        // Zombified Piglins prefer Golden Spears.
        if (mob instanceof ZombifiedPiglinEntity) {

            if (mob.getRandom().nextFloat() < 0.12F) {
                equipSpear(mob, ModItems.GOLDEN_SPEAR);
            }

            return;
        }

        // Husks mostly receive Stone and Iron Spears.
        if (mob instanceof HuskEntity) {

            if (mob.getRandom().nextFloat() >= 0.08F) {
                return;
            }

            int roll = mob.getRandom().nextInt(100);

            if (roll < 55) {
                equipSpear(mob, ModItems.STONE_SPEAR);
            } else if (roll < 90) {
                equipSpear(mob, ModItems.IRON_SPEAR);
            } else {
                equipSpear(mob, ModItems.GOLDEN_SPEAR);
            }

            return;
        }

        // Normal Zombies receive a mixture of basic spears.
        if (mob.getClass() == ZombieEntity.class) {

            if (mob.getRandom().nextFloat() >= 0.05F) {
                return;
            }

            int roll = mob.getRandom().nextInt(100);

            if (roll < 50) {
                equipSpear(mob, ModItems.WOODEN_SPEAR);
            } else if (roll < 85) {
                equipSpear(mob, ModItems.STONE_SPEAR);
            } else {
                equipSpear(mob, ModItems.IRON_SPEAR);
            }
        }
    }

    public static void equipPiglin(PiglinEntity mob) {

        if (mob.world.isClient || mob.isBaby()) {
            return;
        }

        // Piglins have a strong preference for golden equipment.
        if (mob.getRandom().nextFloat() < 0.15F) {
            equipSpear(mob, ModItems.GOLDEN_SPEAR);
        }
    }

    private static void equipSpear(MobEntity mob, Item item) {

        ItemStack spear = new ItemStack(item);

        mob.equipStack(
                EquipmentSlot.MAINHAND,
                spear
        );

        // Small chance for the spear to drop when its mob is killed.
        mob.setEquipmentDropChance(
                EquipmentSlot.MAINHAND,
                0.085F
        );
    }

    // =================================================
    // TARGET RETRIEVAL
    // =================================================

    private static LivingEntity getCombatTarget(MobEntity mob) {

        // Piglins use Brain memories instead of relying exclusively
        // on MobEntity.getTarget().
        if (mob instanceof PiglinEntity) {

            PiglinEntity piglin = (PiglinEntity) mob;

            Optional<LivingEntity> target =
                    piglin.getBrain().getOptionalMemory(
                            MemoryModuleType.ATTACK_TARGET
                    );

            return target.orElse(null);
        }

        return mob.getTarget();
    }

    // =================================================
    // SERVER-SIDE MOB AI
    // =================================================

    public static void tick(MobEntity mob) {

        if (mob == null
                || mob.world.isClient
                || !mob.isAlive()
                || !isSupportedMob(mob)) {
            return;
        }

        ItemStack stack = mob.getMainHandStack();

        // Vanilla behavior is retained when a mob does not hold a spear.
        if (!(stack.getItem() instanceof SpearItem)) {

            MobState previous = STATES.remove(mob);

            if (previous != null
                    && previous.charging
                    && mob.isUsingItem()) {
                mob.stopUsingItem();
            }

            return;
        }

        MobState state = STATES.computeIfAbsent(
                mob,
                entity -> new MobState()
        );

        LivingEntity target = getCombatTarget(mob);

        if (target == null
                || !target.isAlive()
                || target.isSpectator()
                || target.world != mob.world) {

            resetCharge(mob, state);
            STATES.remove(mob);
            return;
        }

        long now = mob.world.getTime();

        // Handle a changed target without continuing an old charge.
        if (state.targetId != null
                && !state.targetId.equals(target.getUuid())) {

            resetCharge(mob, state);
            state.targetId = target.getUuid();
            state.cooldownUntil = now + 5L;
            return;
        }

        if (now < state.cooldownUntil) {
            return;
        }

        double distanceSquared = mob.squaredDistanceTo(target);

        mob.getLookControl().lookAt(
                target,
                30.0F,
                30.0F
        );

        SpearItem spear = (SpearItem) stack.getItem();
        SpearMaterial material = spear.getMaterial();

        // Start a charge when the enemy comes within range.
        if (!state.charging) {

            if (distanceSquared > CHARGE_START_RANGE_SQUARED) {
                return;
            }

            state.charging = true;
            state.chargeStartTick = now;
            state.targetId = target.getUuid();
            state.lastNavigationTick = now;

            mob.setCurrentHand(Hand.MAIN_HAND);
        }

        // Abort if the target escapes.
        if (distanceSquared > CHARGE_ABORT_RANGE_SQUARED) {

            resetCharge(mob, state);
            state.cooldownUntil = now + 10L;

            return;
        }

        // Continue pursuing the target during the charge.
        if (now - state.lastNavigationTick
                >= NAVIGATION_UPDATE_INTERVAL
                && distanceSquared > 9.0D) {

            mob.getNavigation().startMovingTo(
                    target,
                    1.12D
            );

            state.lastNavigationTick = now;
        }

        int elapsedTicks = (int) Math.min(
                Integer.MAX_VALUE,
                Math.max(0L, now - state.chargeStartTick)
        );

        if (elapsedTicks > material.getTotalChargeTicks()) {

            resetCharge(mob, state);
            state.cooldownUntil = now + 15L;

            return;
        }

        int minimumCharge = Math.max(
                MINIMUM_CHARGE_TICKS,
                material.getActivationDelayTicks()
        );

        if (elapsedTicks >= minimumCharge
                && distanceSquared <= ATTACK_RANGE_SQUARED) {

            performChargeAttack(
                    mob,
                    target,
                    stack,
                    material,
                    elapsedTicks,
                    state,
                    now
            );
        }
    }

    // =================================================
    // MOB CHARGE IMPACT
    // =================================================

    private static void performChargeAttack(
            MobEntity mob,
            LivingEntity target,
            ItemStack stack,
            SpearMaterial material,
            int elapsedTicks,
            MobState state,
            long currentTick
    ) {

        SpearChargeState chargeState =
                SpearChargeState.fromElapsedTicks(
                        material,
                        elapsedTicks
                );

        if (chargeState == SpearChargeState.IDLE) {
            resetCharge(mob, state);
            return;
        }

        Vec3d direction = new Vec3d(
                target.getX() - mob.getX(),
                target.getEyeY() - mob.getEyeY(),
                target.getZ() - mob.getZ()
        ).normalize();

        if (direction.lengthSquared() < 1.0E-8D) {
            resetCharge(mob, state);
            return;
        }

        // Relative movement along the attack direction, in blocks/second.
        double relativeSpeed = mob.getVelocity()
                .subtract(target.getVelocity())
                .dotProduct(direction) * 20.0D;

        int kineticDamage = 1 + (int) Math.floor(
                Math.max(0.0D, relativeSpeed)
                        * material.getChargeMultiplier()
        );

        // Preserve at least the material's ordinary jab damage.
        float baseDamage = Math.max(
                material.getJabDamage(),
                kineticDamage
        );

        float enchantmentDamage = EnchantmentHelper.getAttackDamage(
                stack,
                target.getGroup()
        );

        float finalDamage = baseDamage + enchantmentDamage;

        mob.swingHand(Hand.MAIN_HAND);

        boolean damaged = target.damage(
                DamageSource.mob(mob),
                finalDamage
        );

        if (damaged) {

            // Fire Aspect, Bane and other enchantment callbacks.
            EnchantmentHelper.onTargetDamaged(mob, target);

            // Engaged stage can dismount mounted targets.
            if (chargeState == SpearChargeState.ENGAGED
                    && relativeSpeed >= material.getDismountSpeed()
                    && target.hasVehicle()) {

                target.stopRiding();
            }

            // Engaged and Tired stages produce knockback.
            if (chargeState != SpearChargeState.DISENGAGED) {

                double knockbackX = mob.getX() - target.getX();
                double knockbackZ = mob.getZ() - target.getZ();

                target.takeKnockback(
                        0.6F,
                        knockbackX,
                        knockbackZ
                );
            }

            // One durability point per successful spear impact.
            stack.damage(
                    1,
                    mob,
                    entity -> mob.sendEquipmentBreakStatus(
                            EquipmentSlot.MAINHAND
                    )
            );

            // Mob-specific charge impact feedback.
            if (mob.world instanceof ServerWorld) {

                ServerWorld world = (ServerWorld) mob.world;

                world.playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.ITEM_TRIDENT_HIT,
                        SoundCategory.HOSTILE,
                        0.8F,
                        0.9F
                );

                world.spawnParticles(
                        ParticleTypes.CRIT,
                        target.getX(),
                        target.getY() + target.getHeight() * 0.5D,
                        target.getZ(),
                        8,
                        0.25D,
                        0.25D,
                        0.25D,
                        0.08D
                );
            }
        }

        // Finish the charge and enforce the next attack cooldown.
        mob.stopUsingItem();
        state.charging = false;
        state.cooldownUntil = currentTick + ATTACK_COOLDOWN_TICKS;
        state.targetId = target.getUuid();
        state.lastNavigationTick = currentTick;
    }

    private static void resetCharge(
            MobEntity mob,
            MobState state
    ) {

        if (state.charging && mob.isUsingItem()) {
            mob.stopUsingItem();
        }

        state.charging = false;
        state.chargeStartTick = 0L;
        state.lastNavigationTick = 0L;
        state.targetId = null;
    }

    private static final class MobState {

        private boolean charging;

        private long chargeStartTick;
        private long cooldownUntil;
        private long lastNavigationTick;

        private java.util.UUID targetId;
    }
}
