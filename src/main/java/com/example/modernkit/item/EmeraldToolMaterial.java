package com.example.modernkit.item;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class EmeraldToolMaterial implements ToolMaterial {
    // ---- tunable stats ----
    public static final int DURABILITY = 1800;
    public static final int MINING_LEVEL = 3;              // mines obsidian, not ancient debris
    public static final int ENCHANTABILITY = 22;
    public static final float MATERIAL_ATTACK_DAMAGE = 3.0F;

    // total damage = 1 (bare hand) + bonus + MATERIAL_ATTACK_DAMAGE
    public static final int SWORD_ATTACK_BONUS = 8;         // 1 + 8 + 3 = 12 damage
    public static final float SWORD_ATTACK_SPEED = -3.2F;   // 4.0 - 3.2 = 0.8
    public static final int PICKAXE_ATTACK_BONUS = 2;       // 1 + 2 + 3 = 6 damage
    public static final float PICKAXE_ATTACK_SPEED = -2.8F; // 1.2
    public static final float AXE_ATTACK_BONUS = 6.0F;      // 1 + 6 + 3 = 10 damage
    public static final float AXE_ATTACK_SPEED = -3.0F;     // 1.0
    public static final float SHOVEL_ATTACK_BONUS = 1.0F;   // 1 + 1 + 3 = 5 damage
    public static final float SHOVEL_ATTACK_SPEED = -3.0F;  // 1.0

    // mining speed is set per tool (diamond is 8)
    public static final EmeraldToolMaterial SWORD = new EmeraldToolMaterial(8.0F);
    public static final EmeraldToolMaterial PICKAXE = new EmeraldToolMaterial(14.0F);
    public static final EmeraldToolMaterial AXE = new EmeraldToolMaterial(16.0F);
    public static final EmeraldToolMaterial SHOVEL = new EmeraldToolMaterial(20.0F);

    private final float miningSpeed;

    private EmeraldToolMaterial(float miningSpeed) {
        this.miningSpeed = miningSpeed;
    }

    @Override public int getDurability() { return DURABILITY; }
    @Override public float getMiningSpeedMultiplier() { return miningSpeed; }
    @Override public float getAttackDamage() { return MATERIAL_ATTACK_DAMAGE; }
    @Override public int getMiningLevel() { return MINING_LEVEL; }
    @Override public int getEnchantability() { return ENCHANTABILITY; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.EMERALD);
    }
}
