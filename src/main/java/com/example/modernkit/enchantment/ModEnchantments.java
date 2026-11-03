package com.example.modernkit.enchantment;

import com.example.modernkit.ModernKitMod;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Registration for ModernKit enchantments.
 */
public final class ModEnchantments {

    public static Enchantment LUNGE;

    private ModEnchantments() {
    }

    public static void register() {

        if (LUNGE != null) {
            return;
        }

        LUNGE = Registry.register(
                Registry.ENCHANTMENT,
                new Identifier(ModernKitMod.MOD_ID, "lunge"),
                new LungeEnchantment()
        );
    }
}
