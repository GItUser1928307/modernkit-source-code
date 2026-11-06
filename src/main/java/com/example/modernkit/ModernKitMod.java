package com.example.modernkit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.modernkit.item.ModItems;
import com.example.modernkit.item.RainbowItems;
import com.example.modernkit.entity.ModEntities;
import com.example.modernkit.loot.SpearWorldLoot;
import com.example.modernkit.enchantment.ModEnchantments;
import com.example.modernkit.network.SpearPackets;
import com.example.modernkit.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

public class ModernKitMod implements ModInitializer {
    public static final String MOD_ID = "modernkit";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEntities.register();
        ModSounds.register();
        ModItems.register();
        RainbowItems.register();
        ModEnchantments.register();
        SpearWorldLoot.register();
        SpearPackets.register();
    }
}
