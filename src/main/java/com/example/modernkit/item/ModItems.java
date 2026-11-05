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
    public static final Item WIND_CHARGE = new WindChargeItem();

    // ModernKit spear collection
    public static final SpearItem WOODEN_SPEAR =
            new SpearItem(SpearMaterial.WOODEN);

    public static final SpearItem STONE_SPEAR =
            new SpearItem(SpearMaterial.STONE);

    public static final SpearItem GOLDEN_SPEAR =
            new SpearItem(SpearMaterial.GOLDEN);

    public static final SpearItem IRON_SPEAR =
            new SpearItem(SpearMaterial.IRON);

    public static final SpearItem DIAMOND_SPEAR =
            new SpearItem(SpearMaterial.DIAMOND);

    public static final SpearItem NETHERITE_SPEAR =
            new SpearItem(SpearMaterial.NETHERITE);

    public static void register() {
        register("bedrock_pickaxe", BEDROCK_PICKAXE);
        register("heavy_core", HEAVY_CORE);
        register("breeze_rod", BREEZE_ROD);
        register("mace", MACE);
        register("wind_charge", WIND_CHARGE);

        // Register all six spear variants
        register("wooden_spear", WOODEN_SPEAR);
        register("stone_spear", STONE_SPEAR);
        register("golden_spear", GOLDEN_SPEAR);
        register("iron_spear", IRON_SPEAR);
        register("diamond_spear", DIAMOND_SPEAR);
        register("netherite_spear", NETHERITE_SPEAR);
    }

    private static void register(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier(ModernKitMod.MOD_ID, name), item);
    }
}
