package com.example.modernkit.mixin;

import com.example.modernkit.item.SpearItem;
import com.example.modernkit.network.SpearPackets;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Detects spear jab input and prevents spear block mining.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    protected int attackCooldown;

    @Inject(
            method = "doAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modernkit$handleSpearJab(CallbackInfo ci) {

        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.player == null || client.currentScreen != null) {
            return;
        }

        if (!(client.player.getMainHandStack().getItem() instanceof SpearItem)) {
            return;
        }

        // Enforce the local material-specific attack cooldown.
        SpearItem spear = (SpearItem) client.player.getMainHandStack().getItem();

        if (this.attackCooldown > 0) {
            ci.cancel();
            return;
        }

        this.attackCooldown = Math.max(
                1,
                Math.round(20.0F / spear.getMaterial().getAttackSpeed())
        );

        // Immediate local animation.
        ((LivingEntityInvoker) client.player)
                .modernkit$invokeSwingHand(Hand.MAIN_HAND);

        // Ask the server to validate and execute the jab.
        if (client.getNetworkHandler() != null
                && ClientPlayNetworking.canSend(SpearPackets.SPEAR_JAB_PACKET)) {

            ClientPlayNetworking.send(
                    SpearPackets.SPEAR_JAB_PACKET,
                    PacketByteBufs.empty()
            );
        }

        // Prevent the ordinary Minecraft attack from running.
        ci.cancel();
    }

    @Inject(
            method = "handleBlockBreaking",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modernkit$preventSpearBlockMining(
            boolean breaking,
            CallbackInfo ci
    ) {

        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.player != null
                && client.player.getMainHandStack().getItem() instanceof SpearItem) {

            ci.cancel();
        }
    }
}
