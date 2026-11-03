package com.example.modernkit.mixin;

import com.example.modernkit.item.SpearItem;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the vanilla sword-compatible weapon enchantments
 * available to ModernKit spears.
 *
 * Sweeping Edge is deliberately excluded.
 */
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Inject(
            method = "isAcceptableItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modernkit$allowSpearEnchantments(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (stack == null ||
                !(stack.getItem() instanceof SpearItem)) {
            return;
        }

        Enchantment self = (Enchantment) (Object) this;

        boolean supported =
                self == Enchantments.SHARPNESS
                || self == Enchantments.SMITE
                || self == Enchantments.BANE_OF_ARTHROPODS
                || self == Enchantments.KNOCKBACK
                || self == Enchantments.FIRE_ASPECT
                || self == Enchantments.LOOTING;

        if (supported) {
            cir.setReturnValue(true);
        }

        // Sweeping Edge is intentionally not enabled.
    }
}
