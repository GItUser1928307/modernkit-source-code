package com.example.modernkit.client;

import com.example.modernkit.combat.SpearChargeState;
import com.example.modernkit.item.SpearItem;
import com.example.modernkit.item.SpearMaterial;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3f;

/**
 * Calculates and applies client-side spear animations.
 */
public final class SpearAnimationController {

    private SpearAnimationController() {
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    public static boolean isChargingSpear(LivingEntity entity) {
        if (entity == null || !entity.isUsingItem()) {
            return false;
        }

        return entity.getActiveItem().getItem() instanceof SpearItem;
    }

    /**
     * Returns the current charge state of an entity's active spear.
     */
    public static SpearChargeState getChargeState(LivingEntity entity) {

        if (!isChargingSpear(entity)) {
            return SpearChargeState.IDLE;
        }

        ItemStack stack = entity.getActiveItem();
        SpearMaterial material =
                ((SpearItem) stack.getItem()).getMaterial();

        return SpearChargeState.fromElapsedTicks(
                material,
                entity.getItemUseTime()
        );
    }

    /**
     * Total charge progress, normalized to 0..1.
     */
    public static float getChargeProgress(LivingEntity entity) {

        if (!isChargingSpear(entity)) {
            return 0.0F;
        }

        SpearItem spear = (SpearItem) entity.getActiveItem().getItem();

        int totalTicks = spear.getMaterial().getTotalChargeTicks();

        if (totalTicks <= 0) {
            return 0.0F;
        }

        return clamp(
                (float) entity.getItemUseTime() / (float) totalTicks
        );
    }

    private static float getStageWeight(SpearChargeState state) {

        switch (state) {
            case ENGAGED:
                return 1.0F;

            case TIRED:
                return 0.80F;

            case DISENGAGED:
                return 0.52F;

            default:
                return 0.0F;
        }
    }

    /**
     * First-person item pose.
     *
     * Charging and jab attacks use different transformations.
     */
    public static void applyFirstPersonTransform(
            MatrixStack matrices,
            AbstractClientPlayerEntity player,
            Hand hand,
            float swingProgress,
            ItemStack stack
    ) {

        if (!(stack.getItem() instanceof SpearItem)) {
            return;
        }

        boolean rightHand =
                (hand == Hand.MAIN_HAND)
                        == (player.getMainArm() == Arm.RIGHT);

        float side = rightHand ? 1.0F : -1.0F;

        // ------------------------------------------
        // CHARGE ANIMATION
        // ------------------------------------------

        boolean activelyCharging =
                player.isUsingItem()
                        && player.getActiveHand() == hand
                        && player.getActiveItem().getItem() instanceof SpearItem;

        if (activelyCharging) {

            SpearChargeState state = getChargeState(player);
            float weight = getStageWeight(state);

            float progress = getChargeProgress(player);

            // Bring the spear forward into its charging stance.
            matrices.translate(
                    side * 0.015D * weight,
                    -0.055D * weight,
                    0.12D * weight + 0.025D * progress
            );

            matrices.multiply(
                    Vec3f.POSITIVE_X.getDegreesQuaternion(
                            -12.0F * weight
                    )
            );

            matrices.multiply(
                    Vec3f.POSITIVE_Y.getDegreesQuaternion(
                            side * 4.0F * weight
                    )
            );

            return;
        }

        // ------------------------------------------
        // JAB ANIMATION
        // ------------------------------------------

        if (swingProgress > 0.001F) {

            float jab = (float) Math.sin(
                    swingProgress * Math.PI
            );

            jab = clamp(jab);

            matrices.translate(
                    side * 0.018D * jab,
                    -0.065D * jab,
                    -0.34D * jab
            );

            matrices.multiply(
                    Vec3f.POSITIVE_X.getDegreesQuaternion(
                            19.0F * jab
                    )
            );

            matrices.multiply(
                    Vec3f.POSITIVE_Y.getDegreesQuaternion(
                            -7.0F * side * jab
                    )
            );
        }
    }

    /**
     * Returns the active arm for a player's current charge.
     */
    public static Arm getActiveArm(LivingEntity entity) {

        if (entity.getActiveHand() == Hand.MAIN_HAND) {
            return entity.getMainArm();
        }

        return entity.getMainArm() == Arm.RIGHT
                ? Arm.LEFT
                : Arm.RIGHT;
    }
}
