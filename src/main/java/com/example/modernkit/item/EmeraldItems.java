package com.example.modernkit.item;

import com.example.modernkit.ModernKitMod;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class EmeraldItems {
    private EmeraldItems() {}

    public static final int SHOVEL_AREA = 4; // 4x4

    public static final Item EMERALD_SWORD = new SwordItem(
            EmeraldToolMaterial.SWORD,
            EmeraldToolMaterial.SWORD_ATTACK_BONUS,
            EmeraldToolMaterial.SWORD_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.COMBAT));

    public static final Item EMERALD_PICKAXE = new PickaxeItem(
            EmeraldToolMaterial.PICKAXE,
            EmeraldToolMaterial.PICKAXE_ATTACK_BONUS,
            EmeraldToolMaterial.PICKAXE_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.TOOLS)) {};

    public static final Item EMERALD_AXE = new CustomAxeItem(
            EmeraldToolMaterial.AXE,
            EmeraldToolMaterial.AXE_ATTACK_BONUS,
            EmeraldToolMaterial.AXE_ATTACK_SPEED,
            new Item.Settings().group(ItemGroup.TOOLS));

    public static final Item EMERALD_SHOVEL = new AreaShovelItem(
            EmeraldToolMaterial.SHOVEL,
            EmeraldToolMaterial.SHOVEL_ATTACK_BONUS,
            EmeraldToolMaterial.SHOVEL_ATTACK_SPEED,
            SHOVEL_AREA,
            new Item.Settings().group(ItemGroup.TOOLS));

    public static final Item EMERALD_HELMET = armor(EquipmentSlot.HEAD);
    public static final Item EMERALD_CHESTPLATE = armor(EquipmentSlot.CHEST);
    public static final Item EMERALD_LEGGINGS = armor(EquipmentSlot.LEGS);
    public static final Item EMERALD_BOOTS = armor(EquipmentSlot.FEET);

    private static Item armor(EquipmentSlot slot) {
        return new ArmorItem(EmeraldArmorMaterial.INSTANCE, slot,
                new Item.Settings().group(ItemGroup.COMBAT));
    }

    public static void register() {
        register("emerald_sword", EMERALD_SWORD);
        register("emerald_pickaxe", EMERALD_PICKAXE);
        register("emerald_axe", EMERALD_AXE);
        register("emerald_shovel", EMERALD_SHOVEL);
        register("emerald_helmet", EMERALD_HELMET);
        register("emerald_chestplate", EMERALD_CHESTPLATE);
        register("emerald_leggings", EMERALD_LEGGINGS);
        register("emerald_boots", EMERALD_BOOTS);
    }

    private static void register(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier(ModernKitMod.MOD_ID, name), item);
    }
}
