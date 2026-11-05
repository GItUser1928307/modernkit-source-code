package com.example.modernkit.item;

import com.example.modernkit.entity.WindChargeEntity;
import com.example.modernkit.sound.ModSounds;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class WindChargeItem extends Item {

    private static final int COOLDOWN_TICKS = 10;

    public WindChargeItem() {
        super(new Item.Settings()
                .group(ItemGroup.COMBAT)
                .maxCount(64));
    }

    @Override
    public TypedActionResult<ItemStack> use(
            World world,
            PlayerEntity user,
            Hand hand
    ) {
        ItemStack stack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

        if (!world.isClient) {
            WindChargeEntity projectile =
                    new WindChargeEntity(world, user);
            projectile.setItem(new ItemStack(this));
            projectile.setProperties(
                    user,
                    user.getPitch(1.0F),
                    user.getYaw(1.0F),
                    0.0F,
                    1.5F,
                    1.0F
            );
            world.spawnEntity(projectile);
            world.playSound(
                    null,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    ModSounds.WIND_CHARGE_THROW,
                    SoundCategory.PLAYERS,
                    0.5F,
                    0.9F + world.random.nextFloat() * 0.2F
            );
        }

        if (!user.isCreative()) {
            stack.decrement(1);
        }

        return TypedActionResult.success(stack, world.isClient);
    }
}
