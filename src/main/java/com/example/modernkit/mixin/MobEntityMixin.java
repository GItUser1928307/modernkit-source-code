package com.example.modernkit.mixin;

import com.example.modernkit.combat.SpearMobCombat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds server-side spear combat to supported mobs.
 *
 * Prevents the ordinary melee attack from bypassing the
 * custom spear charge controller.
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void modernkit$tickSpearMobCombat(CallbackInfo ci) {

        SpearMobCombat.tick(
                (MobEntity) (Object) this
        );
    }

    @Inject(
            method = "tryAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modernkit$interceptSpearMobAttack(
            Entity target,
            CallbackInfoReturnable<Boolean> cir
    ) {

        MobEntity mob = (MobEntity) (Object) this;

        if (SpearMobCombat.shouldOverrideVanillaAttack(mob)) {
            cir.setReturnValue(false);
        }
    }
}
