package com.example.bedrockpickaxe;

import com.example.bedrockpickaxe.item.ModItems;
import net.fabricmc.api.ModInitializer;

public class BedrockPickaxeMod implements ModInitializer {
    public static final String MOD_ID = "bedrockpickaxe";

    @Override
    public void onInitialize() {
        ModItems.register();
    }
}
