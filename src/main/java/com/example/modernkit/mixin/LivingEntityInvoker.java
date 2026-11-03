package com.example.modernkit.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Allows ModernKit to trigger vanilla hand-swing animation.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {

    @Invoker("swingHand")
    void modernkit$invokeSwingHand(Hand hand);
}
