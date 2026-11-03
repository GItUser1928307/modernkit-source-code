package com.example.modernkit.mixin;

import com.example.modernkit.combat.SpearMobCombat;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.entity.SpawnReason;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a chance for Piglins to spawn with Golden Spears.
 */
@Mixin(PiglinEntity.class)
public abstract class PiglinEntityMixin {

    @Inject(
            method = "initialize",
            at = @At("RETURN")
    )
    private void modernkit$equipPiglinSpears(
            ServerWorldAccess world,
            LocalDifficulty difficulty,
            SpawnReason spawnReason,
            EntityData entityData,
            NbtCompound entityNbt,
            CallbackInfoReturnable<EntityData> cir
    ) {

        SpearMobCombat.equipPiglin(
                (PiglinEntity) (Object) this
        );
    }
}
