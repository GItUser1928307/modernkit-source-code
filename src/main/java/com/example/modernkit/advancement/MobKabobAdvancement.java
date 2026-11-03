package com.example.modernkit.advancement;

import com.example.modernkit.ModernKitMod;

import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Server-side grant helper for the Mob Kabob advancement.
 */
public final class MobKabobAdvancement {

    private static final Identifier ADVANCEMENT_ID =
            new Identifier(
                    ModernKitMod.MOD_ID,
                    "adventure/mob_kabob"
            );

    private MobKabobAdvancement() {
    }

    public static void grant(ServerPlayerEntity player) {

        if (player == null || player.server == null) {
            return;
        }

        Advancement advancement =
                player.server.getAdvancementLoader().get(
                        ADVANCEMENT_ID
                );

        if (advancement == null) {
            ModernKitMod.LOGGER.warn(
                    "Mob Kabob advancement could not be found."
            );
            return;
        }

        // The advancement contains a single impossible criterion.
        // It is granted only by our verified five-target charge check.
        player.getAdvancementTracker().grantCriterion(
                advancement,
                "five_mobs"
        );
    }
}
