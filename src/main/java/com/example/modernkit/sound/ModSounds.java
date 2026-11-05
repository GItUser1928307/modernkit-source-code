package com.example.modernkit.sound;

import com.example.modernkit.ModernKitMod;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ModSounds {

    public static SoundEvent SPEAR_JAB_START;
    public static SoundEvent SPEAR_JAB_IMPACT;
    public static SoundEvent SPEAR_CHARGE_ENGAGED;
    public static SoundEvent SPEAR_CHARGE_TIRED;
    public static SoundEvent SPEAR_CHARGE_DISENGAGED;

    private ModSounds() {
    }

    public static void register() {
        if (SPEAR_JAB_START != null) {
            return;
        }

        SPEAR_JAB_START = registerSound("entity.modernkit.spear.jab_start");
        SPEAR_JAB_IMPACT = registerSound("entity.modernkit.spear.jab_impact");
        SPEAR_CHARGE_ENGAGED =
                registerSound("entity.modernkit.spear.charge_engaged");
        SPEAR_CHARGE_TIRED =
                registerSound("entity.modernkit.spear.charge_tired");
        SPEAR_CHARGE_DISENGAGED =
                registerSound("entity.modernkit.spear.charge_disengaged");
    }

    private static SoundEvent registerSound(String path) {
        Identifier id = new Identifier(ModernKitMod.MOD_ID, path);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }
}
