package com.example.modernkit.entity;

import com.example.modernkit.ModernKitMod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ModEntities {

    public static final EntityType<WindChargeEntity> WIND_CHARGE =
            EntityType.Builder.<WindChargeEntity>create(
                            WindChargeEntity::new,
                            SpawnGroup.MISC
                    )
                    .disableSaving()
                    .setDimensions(0.25F, 0.25F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10)
                    .build(ModernKitMod.MOD_ID + ":wind_charge");

    private ModEntities() {
    }

    public static void register() {
        Registry.register(
                Registry.ENTITY_TYPE,
                new Identifier(ModernKitMod.MOD_ID, "wind_charge"),
                WIND_CHARGE
        );
    }
}
