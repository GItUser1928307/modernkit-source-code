package com.example.modernkit.item;

import com.example.modernkit.ModernKitMod;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class RainbowItems {
    private RainbowItems() {}

    public static final Item RAINBOW_INGOT = new Item(
            new Item.Settings().group(ItemGroup.MATERIALS).fireproof());

    public static final Item RAINBOW_SWORD = new SwordItem(
            RainbowToolMaterial.INSTANCE,
            RainbowToolMaterial.SWORD_ATTACK_BONUS,
            RainbowToolMaterial.SWORD_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.COMBAT).fireproof());

    public static final Item RAINBOW_PICKAXE = new RainbowPickaxeItem(
            RainbowToolMaterial.INSTANCE,
            RainbowToolMaterial.PICKAXE_ATTACK_BONUS,
            RainbowToolMaterial.PICKAXE_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.TOOLS).fireproof());

    public static final Item RAINBOW_HELMET = armor(EquipmentSlot.HEAD);
    public static final Item RAINBOW_CHESTPLATE = armor(EquipmentSlot.CHEST);
    public static final Item RAINBOW_LEGGINGS = armor(EquipmentSlot.LEGS);
    public static final Item RAINBOW_BOOTS = armor(EquipmentSlot.FEET);

    private static Item armor(EquipmentSlot slot) {
        return new ArmorItem(RainbowArmorMaterial.INSTANCE, slot,
                new Item.Settings().group(ItemGroup.COMBAT).fireproof());
    }

    public static void register() {
        register("rainbow_ingot", RAINBOW_INGOT);
        register("rainbow_sword", RAINBOW_SWORD);
        register("rainbow_pickaxe", RAINBOW_PICKAXE);
        register("rainbow_helmet", RAINBOW_HELMET);
        register("rainbow_chestplate", RAINBOW_CHESTPLATE);
        register("rainbow_leggings", RAINBOW_LEGGINGS);
        register("rainbow_boots", RAINBOW_BOOTS);
    }

    private static void register(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier(ModernKitMod.MOD_ID, name), item);
    }
}
