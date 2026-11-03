package com.example.modernkit.mixin;

import com.example.modernkit.item.SmashAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Unique
    private boolean modernkit$smashNoFall;

    @Redirect(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean modernkit$smash(Entity target, DamageSource source, float amount) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!SmashAttack.canSmash(self)) {
            return target.damage(source, amount);
        }

        float fall = self.fallDistance;
        boolean hit = target.damage(source, amount + SmashAttack.bonusDamage(fall));

        if (hit && !self.world.isClient) {
            // Phase 3: effects
            SmashAttack.doEffects(self, target, fall);
            SmashAttack.knockbackNearby(self, target, fall);
            self.fallDistance = 0.0F;
            this.modernkit$smashNoFall = true;
            if (self instanceof ServerPlayerEntity) {
                self.setVelocity(self.getVelocity().x, 0.01D, self.getVelocity().z);
                ((ServerPlayerEntity) self).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(self));
            }
        }
        return hit;
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void modernkit$cancelFall(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.modernkit$smashNoFall && source == DamageSource.FALL) {
            this.modernkit$smashNoFall = false;
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void modernkit$clearNoFall(CallbackInfo ci) {
        if (this.modernkit$smashNoFall && ((PlayerEntity) (Object) this).isOnGround()) {
            this.modernkit$smashNoFall = false;
        }
    }
}
