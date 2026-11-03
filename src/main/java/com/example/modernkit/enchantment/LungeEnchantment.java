package com.example.modernkit.enchantment;

import com.example.modernkit.item.SpearItem;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Lunge is exclusive to ModernKit spears.
 *
 * Maximum level: III
 * Enchantment weight: 5 (UNCOMMON)
 */
public class LungeEnchantment extends Enchantment {

    public LungeEnchantment() {
        super(
                Enchantment.Rarity.UNCOMMON,
                EnchantmentTarget.WEAPON,
                new EquipmentSlot[] {
                        EquipmentSlot.MAINHAND
                }
        );
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinPower(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxPower(int level) {
        return 25 + (level - 1) * 8;
    }

    /**
     * Only ModernKit spear items can receive Lunge.
     */
    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof SpearItem;
    }
}
