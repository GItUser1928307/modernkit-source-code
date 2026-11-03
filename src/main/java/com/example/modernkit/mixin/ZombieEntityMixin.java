package com.example.modernkit.mixin;

import com.example.modernkit.combat.SpearMobCombat;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.entity.SpawnReason;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds spear equipment after Zombie-family spawn initialization.
 *
 * This also covers inherited initialization for Husks and
 * Zombified Piglins.
 */
@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin {

    @Inject(
            method = "initialize",
            at = @At("RETURN")
    )
    private void modernkit$equipZombieSpears(
            ServerWorldAccess world,
            LocalDifficulty difficulty,
            SpawnReason spawnReason,
            EntityData entityData,
            NbtCompound entityNbt,
            CallbackInfoReturnable<EntityData> cir
    ) {

        SpearMobCombat.equipZombieFamily(
                (ZombieEntity) (Object) this
        );
    }
}
