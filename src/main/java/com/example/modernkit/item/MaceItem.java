package com.example.modernkit.item;

import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class MaceItem extends SwordItem {
    // 1 (base) + 5 = 6 damage, 4.0 - 3.4 = 0.6 attack speed, like the real mace
    public MaceItem(Settings settings) {
        super(MaceMaterial.INSTANCE, 5, -3.4F, settings);
    }

    static final class MaceMaterial implements ToolMaterial {
        static final MaceMaterial INSTANCE = new MaceMaterial();

        @Override public int getDurability() { return 500; }
        @Override public float getMiningSpeedMultiplier() { return 1.0F; }
        @Override public float getAttackDamage() { return 0.0F; }
        @Override public int getMiningLevel() { return 0; }
        @Override public int getEnchantability() { return 15; }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(ModItems.BREEZE_ROD);
        }
    }
}
