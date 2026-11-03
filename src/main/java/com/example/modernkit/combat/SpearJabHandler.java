package com.example.modernkit.combat;

import com.example.modernkit.item.SpearItem;
import com.example.modernkit.combat.SpearLungeHandler;
import com.example.modernkit.mixin.LivingEntityInvoker;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Authoritative server-side implementation of spear jab attacks.
 */
public final class SpearJabHandler {

    private static final double MIN_REACH = 2.0D;
    private static final double MAX_REACH = 4.5D;
    private static final double HITBOX_MARGIN = 0.125D;

    // Initial knockback strength; we can tune it through in-game testing.
    private static final float KNOCKBACK_STRENGTH = 0.4F;

    private static final Map<UUID, Long> LAST_JAB_TICK = new HashMap<>();

    private SpearJabHandler() {
    }

    public static void perform(ServerPlayerEntity player) {

        // Validate the player.
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        ItemStack stack = player.getMainHandStack();

        if (!(stack.getItem() instanceof SpearItem)) {
            return;
        }

        SpearItem spear = (SpearItem) stack.getItem();

        // Material-dependent cooldown.
        int cooldownTicks = Math.max(
                1,
                Math.round(20.0F / spear.getMaterial().getAttackSpeed())
        );

        long currentTick = player.world.getTime();
        UUID playerId = player.getUuid();

        Long previousTick = LAST_JAB_TICK.get(playerId);

        if (previousTick != null
                && currentTick >= previousTick
                && currentTick - previousTick < cooldownTicks) {
            return;
        }

        // Apply cooldown even if the jab misses.
        LAST_JAB_TICK.put(playerId, currentTick);

        // Stage 4: Apply the spear-exclusive Lunge enchantment.
        // This is deliberately called only for a valid spear jab.
        SpearLungeHandler.tryApply(player, stack);


        // Synchronize the hand swing with other clients.
        ((LivingEntityInvoker) player).modernkit$invokeSwingHand(Hand.MAIN_HAND);

        // Calculate the player's looking direction and eye position.
        Vec3d direction = player.getRotationVec(1.0F).normalize();
        Vec3d origin = player.getCameraPosVec(1.0F);

        // Stage 6: spear jab sound and particle trail.
        SpearEffects.playJabStart(player, origin, direction);


        // Check whether a solid block obstructs the jab.
        Vec3d fullRayEnd = origin.add(direction.multiply(MAX_REACH));

        net.minecraft.util.hit.BlockHitResult blockHit =
                player.world.raycast(
                        new net.minecraft.world.RaycastContext(
                                origin,
                                fullRayEnd,
                                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                                player
                        )
                );

        double effectiveReach = MAX_REACH;

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            effectiveReach = Math.min(
                    MAX_REACH,
                    origin.distanceTo(blockHit.getPos())
            );
        }

        // Nothing can be hit if the obstruction is inside minimum reach.
        if (effectiveReach < MIN_REACH) {
            return;
        }

        // The jab only registers targets along this part of the ray.
        Vec3d rayStart = origin.add(direction.multiply(MIN_REACH));
        Vec3d rayEnd = origin.add(direction.multiply(effectiveReach));

        // Broad-phase entity search.
        Box searchBox = player.getBoundingBox()
                .stretch(
                        direction.x * MAX_REACH,
                        direction.y * MAX_REACH,
                        direction.z * MAX_REACH
                )
                .expand(1.0D);

        List<Entity> candidates = player.world.getOtherEntities(
                player,
                searchBox,
                entity ->
                        entity instanceof LivingEntity
                                && ((LivingEntity) entity).isAlive()
                                && !entity.isSpectator()
                                && !player.isTeammate(entity)
        );

        boolean hitAnyTarget = false;

        // Test each entity independently, allowing multiple targets.
        for (Entity candidate : candidates) {

            LivingEntity target = (LivingEntity) candidate;

            boolean intersectsRay = target.getBoundingBox()
                    .expand(HITBOX_MARGIN)
                    .raycast(rayStart, rayEnd)
                    .isPresent();

            if (!intersectsRay) {
                continue;
            }

            // Vanilla sword-compatible enchantment damage.
            float enchantmentBonus = EnchantmentHelper.getAttackDamage(
                    stack,
                    target.getGroup()
            );

            float finalDamage =
                    spear.getMaterial().getJabDamage() + enchantmentBonus;

            boolean damaged = target.damage(
                    DamageSource.player(player),
                    finalDamage
            );

            if (!damaged) {
                continue;
            }

            // Applies Fire Aspect, Bane of Arthropods and other
            // normal vanilla enchantment target effects.
            EnchantmentHelper.onTargetDamaged(player, target);


            // Push the target away from the attacker.
            double knockbackX = player.getX() - target.getX();
            double knockbackZ = player.getZ() - target.getZ();

            // Handle entities occupying almost exactly the same position.
            if (knockbackX * knockbackX + knockbackZ * knockbackZ < 1.0E-7D) {
                knockbackX = -direction.x;
                knockbackZ = -direction.z;
            }

            int knockbackLevel = EnchantmentHelper.getLevel(
                    Enchantments.KNOCKBACK,
                    stack
            );

            target.takeKnockback(
                    KNOCKBACK_STRENGTH + knockbackLevel * 0.5F,
                    knockbackX,
                    knockbackZ
            );

            // Stage 6: successful jab impact effects.
            SpearEffects.playJabImpact(player, target);

            hitAnyTarget = true;
        }

        // Consume one durability point for a successful jab,
        // regardless of how many entities were hit.
        if (hitAnyTarget) {
            stack.damage(
                    1,
                    player,
                    entity -> entity.sendEquipmentBreakStatus(
                            EquipmentSlot.MAINHAND
                    )
            );
        }
    }
}
