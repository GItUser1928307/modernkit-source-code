package com.example.modernkit.network;

import com.example.modernkit.ModernKitMod;
import com.example.modernkit.combat.SpearJabHandler;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

/**
 * Client-to-server spear combat packets.
 */
public final class SpearPackets {

    public static final Identifier SPEAR_JAB_PACKET =
            new Identifier(ModernKitMod.MOD_ID, "spear_jab");

    private SpearPackets() {
    }

    public static void register() {

        ServerPlayNetworking.registerGlobalReceiver(
                SPEAR_JAB_PACKET,
                (server, player, handler, buf, responseSender) -> {

                    // All world modifications happen on the server thread.
                    server.execute(() -> SpearJabHandler.perform(player));
                }
        );
    }
}
