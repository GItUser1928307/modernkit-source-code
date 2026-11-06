package com.example.modernkit.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class RainbowArmorMaterial implements ArmorMaterial {
    public static final RainbowArmorMaterial INSTANCE = new RainbowArmorMaterial();

    // ---- tunable stats (arrays are indexed: boots, leggings, chestplate, helmet) ----
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private static final int[] PROTECTION = {7, 8, 9, 6};      // total 30, the game's cap
    public static final int DURABILITY_MULTIPLIER = 500;
    public static final float TOUGHNESS = 5.0F;                 // 4 pieces = 20, the game's cap
    public static final float KNOCKBACK_RESISTANCE = 0.25F;     // 4 pieces = 100%
    public static final int ENCHANTABILITY = 30;

    @Override
    public int getDurability(EquipmentSlot slot) {
        return BASE_DURABILITY[slot.getEntitySlotId()] * DURABILITY_MULTIPLIER;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return PROTECTION[slot.getEntitySlotId()];
    }

    @Override public int getEnchantability() { return ENCHANTABILITY; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(RainbowItems.RAINBOW_INGOT);
    }

    // must match the armor texture file names: modernkit_rainbow_layer_1.png / _layer_2.png
    @Override public String getName() { return "modernkit_rainbow"; }
    @Override public float getToughness() { return TOUGHNESS; }
    @Override public float getKnockbackResistance() { return KNOCKBACK_RESISTANCE; }
}
