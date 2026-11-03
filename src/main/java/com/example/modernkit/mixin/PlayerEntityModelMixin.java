package com.example.modernkit.mixin;

import com.example.modernkit.client.SpearAnimationController;
import com.example.modernkit.combat.SpearChargeState;
import com.example.modernkit.item.SpearItem;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Third-person spear charging and jab poses.
 */
@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {

    @Inject(
            method = "setAngles",
            at = @At("TAIL")
    )
    private void modernkit$animateThirdPersonSpear(
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float animationProgress,
            float headYaw,
            float headPitch,
            CallbackInfo ci
    ) {

        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) entity;

        BipedEntityModel<?> model =
                (BipedEntityModel<?>) (Object) this;

        // ------------------------------------------
        // CHARGING POSE
        // ------------------------------------------

        if (SpearAnimationController.isChargingSpear(player)) {

            ItemStack activeStack = player.getActiveItem();

            if (!(activeStack.getItem() instanceof SpearItem)) {
                return;
            }

            Arm activeArm =
                    SpearAnimationController.getActiveArm(player);

            ModelPart arm = activeArm == Arm.RIGHT
                    ? model.rightArm
                    : model.leftArm;

            SpearChargeState state =
                    SpearAnimationController.getChargeState(player);

            float weight;

            switch (state) {
                case ENGAGED:
                    weight = 1.0F;
                    break;

                case TIRED:
                    weight = 0.80F;
                    break;

                case DISENGAGED:
                    weight = 0.52F;
                    break;

                default:
                    weight = 0.0F;
                    break;
            }

            float charge =
                    SpearAnimationController.getChargeProgress(player);

            // Raised spear position.
            arm.pitch = -2.28F - 0.20F * weight
                    - 0.06F * charge;

            arm.yaw = activeArm == Arm.RIGHT
                    ? -0.15F
                    : 0.15F;

            arm.roll = activeArm == Arm.RIGHT
                    ? -0.08F
                    : 0.08F;

            return;
        }

        // ------------------------------------------
        // THIRD-PERSON JAB SWING
        // ------------------------------------------

        ItemStack mainHand = player.getMainHandStack();

        if (!(mainHand.getItem() instanceof SpearItem)) {
            return;
        }

        float swing = player.getHandSwingProgress(1.0F);

        if (swing <= 0.001F) {
            return;
        }

        float jab = (float) Math.sin(
                swing * Math.PI
        );

        Arm mainArm = player.getMainArm();

        ModelPart arm = mainArm == Arm.RIGHT
                ? model.rightArm
                : model.leftArm;

        arm.pitch -= 0.32F * jab;

        arm.yaw += mainArm == Arm.RIGHT
                ? -0.07F * jab
                : 0.07F * jab;
    }
}
