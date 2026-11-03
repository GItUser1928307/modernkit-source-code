package com.example.modernkit;

import com.example.modernkit.item.ModItems;
import net.fabricmc.api.ModInitializer;

public class ModernKitMod implements ModInitializer {
    public static final String MOD_ID = "modernkit";

    @Override
    public void onInitialize() {
        ModItems.register();
    }
}
