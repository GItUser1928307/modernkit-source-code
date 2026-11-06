package com.example.modernkit.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class RainbowToolMaterial implements ToolMaterial {
    public static final RainbowToolMaterial INSTANCE = new RainbowToolMaterial();

    // ---- tunable stats ----
    public static final int DURABILITY = 100_000;
    public static final float MINING_SPEED = 300.0F;
    public static final float MATERIAL_ATTACK_DAMAGE = 4.0F;
    public static final int MINING_LEVEL = 4;
    public static final int ENCHANTABILITY = 30;

    // total damage = 1 (bare hand) + bonus + MATERIAL_ATTACK_DAMAGE
    public static final int SWORD_ATTACK_BONUS = 25;        // 1 + 25 + 4 = 30 damage
    public static final float SWORD_ATTACK_SPEED = 2.0F;    // 4.0 + 2.0 = 6.0 attacks per second
    public static final int PICKAXE_ATTACK_BONUS = 5;       // 1 + 5 + 4 = 10 damage
    public static final float PICKAXE_ATTACK_SPEED = 0.0F;  // 4.0 attacks per second

    @Override public int getDurability() { return DURABILITY; }
    @Override public float getMiningSpeedMultiplier() { return MINING_SPEED; }
    @Override public float getAttackDamage() { return MATERIAL_ATTACK_DAMAGE; }
    @Override public int getMiningLevel() { return MINING_LEVEL; }
    @Override public int getEnchantability() { return ENCHANTABILITY; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(RainbowItems.RAINBOW_INGOT);
    }
}
