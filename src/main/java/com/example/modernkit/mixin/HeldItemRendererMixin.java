package com.example.modernkit.mixin;

import com.example.modernkit.client.SpearAnimationController;
import com.example.modernkit.item.SpearItem;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Custom first-person spear animation transformations.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Unique
    private boolean modernkit$spearMatrixPushed;

    @Inject(
            method = "renderFirstPersonItem",
            at = @At("HEAD")
    )
    private void modernkit$applyFirstPersonSpearAnimation(
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {

        if (!(item.getItem() instanceof SpearItem)) {
            return;
        }

        matrices.push();
        modernkit$spearMatrixPushed = true;

        SpearAnimationController.applyFirstPersonTransform(
                matrices,
                player,
                hand,
                swingProgress,
                item
        );
    }

    @Inject(
            method = "renderFirstPersonItem",
            at = @At("RETURN")
    )
    private void modernkit$restoreFirstPersonSpearMatrix(
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {

        if (modernkit$spearMatrixPushed) {
            matrices.pop();
            modernkit$spearMatrixPushed = false;
        }
    }
}
