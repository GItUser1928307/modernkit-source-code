package com.example.modernkit.client;

import com.example.modernkit.entity.ModEntities;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class WindChargeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(
                ModEntities.WIND_CHARGE,
                (dispatcher, context) ->
                        new FlyingItemEntityRenderer<>(
                                dispatcher,
                                context.getItemRenderer()
                        )
        );
    }
}
