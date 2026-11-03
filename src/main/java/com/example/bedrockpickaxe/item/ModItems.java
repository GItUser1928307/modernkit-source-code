package com.example.bedrockpickaxe.item;

import com.example.bedrockpickaxe.BedrockPickaxeMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {
    public static final Item BEDROCK_PICKAXE = new BedrockPickaxeItem(
            BedrockToolMaterial.INSTANCE, 1, -2.8F,
            new Item.Settings().group(ItemGroup.TOOLS).fireproof());

    public static void register() {
        Registry.register(Registry.ITEM, new Identifier(BedrockPickaxeMod.MOD_ID, "bedrock_pickaxe"), BEDROCK_PICKAXE);
    }
}
