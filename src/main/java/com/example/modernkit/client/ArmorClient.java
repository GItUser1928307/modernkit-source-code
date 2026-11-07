package com.example.modernkit.client;

import com.example.modernkit.item.EmeraldItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderingRegistry;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ArmorClient implements ClientModInitializer {
    private static EmeraldArmorModel outer;
    private static EmeraldArmorModel inner;

    @Override
    public void onInitializeClient() {
        ArmorRenderingRegistry.registerModel(ArmorClient::emeraldModel,
                EmeraldItems.EMERALD_HELMET,
                EmeraldItems.EMERALD_CHESTPLATE,
                EmeraldItems.EMERALD_LEGGINGS,
                EmeraldItems.EMERALD_BOOTS);
    }

    private static BipedEntityModel<LivingEntity> emeraldModel(LivingEntity entity, ItemStack stack,
                                                               EquipmentSlot slot, BipedEntityModel<LivingEntity> defaultModel) {
        if (slot == EquipmentSlot.LEGS) {
            if (inner == null) {
                inner = new EmeraldArmorModel(0.5F, true);
            }
            return inner;
        }
        if (outer == null) {
            outer = new EmeraldArmorModel(1.0F, false);
        }
        return outer;
    }
}
