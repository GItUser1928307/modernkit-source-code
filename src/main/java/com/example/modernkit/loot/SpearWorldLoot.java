package com.example.modernkit.loot;

import com.example.modernkit.ModernKitMod;
import com.example.modernkit.enchantment.ModEnchantments;
import com.example.modernkit.item.ModItems;

import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

/**
 * Adds spear equipment and Lunge books to selected vanilla
 * structure loot tables without replacing their existing contents.
 */
public final class SpearWorldLoot {

    private SpearWorldLoot() {
    }

    public static void register() {

        LootTableLoadingCallback.EVENT.register(
                (resourceManager, manager, id, supplier, setter) -> {

                    // --------------------------------------
                    // IRON SPEAR STRUCTURE LOOT
                    // --------------------------------------

                    if (matches(
                            id,
                            "minecraft:chests/buried_treasure"
                    )) {
                        supplier.pool(
                                spearPool(ModItems.IRON_SPEAR, 0.10F)
                        );
                    }

                    if (matches(
                            id,
                            "minecraft:chests/village/village_weaponsmith"
                    )) {
                        supplier.pool(
                                spearPool(ModItems.IRON_SPEAR, 0.08F)
                        );
                    }

                    // --------------------------------------
                    // LUNGE ENCHANTED BOOKS
                    // --------------------------------------

                    if (matches(id, "minecraft:chests/simple_dungeon")) {
                        supplier.pool(lungeBookPool(0.06F));
                    }

                    if (matches(id, "minecraft:chests/desert_pyramid")) {
                        supplier.pool(lungeBookPool(0.10F));
                    }

                    if (matches(id, "minecraft:chests/jungle_temple")) {
                        supplier.pool(lungeBookPool(0.08F));
                    }

                    if (matches(id, "minecraft:chests/stronghold_library")) {
                        supplier.pool(lungeBookPool(0.12F));
                    }

                    if (matches(id, "minecraft:chests/bastion_treasure")) {
                        supplier.pool(lungeBookPool(0.10F));
                    }

                    if (matches(id, "minecraft:chests/end_city_treasure")) {
                        supplier.pool(lungeBookPool(0.12F));
                    }
                }
        );

        ModernKitMod.LOGGER.info(
                "ModernKit spear structure loot registered."
        );
    }

    private static boolean matches(
            Identifier actual,
            String expected
    ) {
        return actual.equals(new Identifier(expected));
    }

    /**
     * Adds one extra optional spear pool to an existing table.
     */
    private static LootPool.Builder spearPool(
            Item item,
            float chance
    ) {

        return LootPool.builder()
                .with(ItemEntry.builder(item))
                .conditionally(
                        RandomChanceLootCondition.builder(chance)
                );
    }

    /**
     * Stores a custom Lunge enchantment on a vanilla enchanted book.
     */
    private static NbtCompound lungeBookTag(int level) {

        NbtCompound enchantment = new NbtCompound();

        enchantment.putString(
                "id",
                ModernKitMod.MOD_ID + ":lunge"
        );

        enchantment.putShort(
                "lvl",
                (short) level
        );

        NbtList storedEnchantments = new NbtList();

        storedEnchantments.add(enchantment);

        NbtCompound bookTag = new NbtCompound();

        bookTag.put(
                "StoredEnchantments",
                storedEnchantments
        );

        return bookTag;
    }

    /**
     * Adds a randomly weighted Lunge I, II or III book.
     */
    private static LootPool.Builder lungeBookPool(float chance) {

        LootPool.Builder pool = LootPool.builder()
                .conditionally(
                        RandomChanceLootCondition.builder(chance)
                );

        // Lunge I is most common.
        pool.with(
                ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(6)
                        .apply(
                                SetNbtLootFunction.builder(
                                        lungeBookTag(1)
                                )
                        )
        );

        pool.with(
                ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(3)
                        .apply(
                                SetNbtLootFunction.builder(
                                        lungeBookTag(2)
                                )
                        )
        );

        pool.with(
                ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .weight(1)
                        .apply(
                                SetNbtLootFunction.builder(
                                        lungeBookTag(3)
                                )
                        )
        );

        return pool;
    }
}
