package com.example.modernkit.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class EmeraldArmorMaterial implements ArmorMaterial {
    public static final EmeraldArmorMaterial INSTANCE = new EmeraldArmorMaterial();

    // ---- tunable stats (arrays are indexed: boots, leggings, chestplate, helmet) ----
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private static final int[] PROTECTION = {3, 6, 7, 3};       // total 19 (diamond is 20)
    public static final int DURABILITY_MULTIPLIER = 38;
    public static final float TOUGHNESS = 2.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.05F;
    public static final int ENCHANTABILITY = 25;

    @Override
    public int getDurability(EquipmentSlot slot) {
        return BASE_DURABILITY[slot.getEntitySlotId()] * DURABILITY_MULTIPLIER;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return PROTECTION[slot.getEntitySlotId()];
    }

    @Override public int getEnchantability() { return ENCHANTABILITY; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.EMERALD);
    }

    // must match the armor texture file names: modernkit_emerald_layer_1.png / _layer_2.png
    @Override public String getName() { return "modernkit_emerald"; }
    @Override public float getToughness() { return TOUGHNESS; }
    @Override public float getKnockbackResistance() { return KNOCKBACK_RESISTANCE; }
}
