package com.example.modernkit.combat;

import com.example.modernkit.enchantment.ModEnchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Applies the Lunge effect when a spear performs a jab.
 *
 * The server calculates the velocity and resource costs.
 */
public final class SpearLungeHandler {

    private static final double BASE_LUNGE_SPEED = 0.458D;

    private static final int MINIMUM_FOOD_LEVEL = 6;

    private SpearLungeHandler() {
    }

    /**
     * @return true if Lunge was successfully activated.
     */
    public static boolean tryApply(
            ServerPlayerEntity player,
            ItemStack stack
    ) {

        if (player == null || stack == null || stack.isEmpty()) {
            return false;
        }

        // Read Lunge I, II, or III from the actual spear stack.
        int level = EnchantmentHelper.getLevel(
                ModEnchantments.LUNGE,
                stack
        );

        if (level <= 0) {
            return false;
        }

        // No Lunge while touching water.
        if (player.isTouchingWater()) {
            return false;
        }

        // No Lunge while gliding with Elytra.
        if (player.isFallFlying()) {
            return false;
        }

        // No Lunge while mounted.
        if (player.hasVehicle()) {
            return false;
        }

        // Require sufficient hunger outside Creative mode.
        if (!player.isCreative()
                && player.getHungerManager().getFoodLevel()
                < MINIMUM_FOOD_LEVEL) {
            return false;
        }

        // Get the player's viewing direction.
        Vec3d look = player.getRotationVec(1.0F);

        // Apply only the horizontal component.
        // Looking directly forward produces the maximum boost.
        // Looking upward or downward reduces the horizontal boost.
        Vec3d horizontalBoost = new Vec3d(
                look.x,
                0.0D,
                look.z
        ).multiply(BASE_LUNGE_SPEED * level);

        if (horizontalBoost.lengthSquared() < 1.0E-8D) {
            return false;
        }

        // Preserve existing movement, including vertical velocity.
        Vec3d newVelocity = player.getVelocity().add(horizontalBoost);

        player.setVelocity(newVelocity);

        // Synchronize the new velocity with the player's client.
        player.networkHandler.sendPacket(
                new EntityVelocityUpdateS2CPacket(player)
        );

        if (!player.isCreative()) {

            // Exhaustion: 4 points per enchantment level.
            player.addExhaustion(4.0F * level);

            // One additional durability point for Lunge.
            stack.damage(
                    1,
                    player,
                    entity -> entity.sendEquipmentBreakStatus(
                            EquipmentSlot.MAINHAND
                    )
            );
        }

        return true;
    }
}
