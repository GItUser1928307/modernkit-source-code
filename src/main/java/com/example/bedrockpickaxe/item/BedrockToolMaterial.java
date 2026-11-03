package com.example.bedrockpickaxe.item;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class BedrockToolMaterial implements ToolMaterial {
    public static final BedrockToolMaterial INSTANCE = new BedrockToolMaterial();

    @Override
    public int getDurability() {
        return 4000;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 12.0F;
    }

    @Override
    public float getAttackDamage() {
        return 4.0F;
    }

    @Override
    public int getMiningLevel() {
        return 4;
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.BEDROCK);
    }
}
