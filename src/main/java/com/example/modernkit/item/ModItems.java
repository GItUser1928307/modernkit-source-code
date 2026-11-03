package com.example.modernkit.item;

import com.example.modernkit.ModernKitMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {
    public static final Item BEDROCK_PICKAXE = new BedrockPickaxeItem(
            BedrockToolMaterial.INSTANCE, 1, -2.8F,
            new Item.Settings().group(ItemGroup.TOOLS).fireproof());

    public static final Item HEAVY_CORE = new Item(new Item.Settings().group(ItemGroup.MISC));
    public static final Item BREEZE_ROD = new Item(new Item.Settings().group(ItemGroup.MISC));
    public static final Item MACE = new MaceItem(new Item.Settings().group(ItemGroup.COMBAT));

    public static void register() {
        register("bedrock_pickaxe", BEDROCK_PICKAXE);
        register("heavy_core", HEAVY_CORE);
        register("breeze_rod", BREEZE_ROD);
        register("mace", MACE);
    }

    private static void register(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier(ModernKitMod.MOD_ID, name), item);
    }
}
