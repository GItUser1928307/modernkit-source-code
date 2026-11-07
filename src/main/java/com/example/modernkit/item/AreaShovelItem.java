package com.example.modernkit.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Shovel that digs a flat square (areaSize x areaSize) of diggable blocks around the block you mine.
 * Looking steeply up/down digs a horizontal square, otherwise a vertical one.
 * Costs 1 durability per dig. Sneak to dig a single block. Survival mode only.
 */
public class AreaShovelItem extends ShovelItem {
    private static boolean breaking = false;
    private final int areaSize;

    public AreaShovelItem(ToolMaterial material, float attackDamage, float attackSpeed, int areaSize, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
        this.areaSize = areaSize;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (breaking) {
            return true; // blocks broken by the area dig cost no extra durability
        }
        boolean result = super.postMine(stack, world, state, pos, miner);
        if (world.isClient || stack.isEmpty() || !(miner instanceof ServerPlayerEntity) || miner.isSneaking()) {
            return result;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) miner;
        int lo = -(areaSize / 2 - 1);
        int hi = areaSize / 2;
        boolean horizontal = Math.abs(player.pitch) > 50.0F;
        Direction facing = player.getHorizontalFacing();

        breaking = true;
        try {
            for (int a = lo; a <= hi; a++) {
                for (int b = lo; b <= hi; b++) {
                    if (a == 0 && b == 0) {
                        continue;
                    }
                    BlockPos target;
                    if (horizontal) {
                        target = pos.add(a, 0, b);
                    } else if (facing.getAxis() == Direction.Axis.X) {
                        target = pos.add(0, b, a);
                    } else {
                        target = pos.add(a, b, 0);
                    }
                    breakIfDiggable(stack, world, player, target);
                }
            }
        } finally {
            breaking = false;
        }
        return result;
    }

    private void breakIfDiggable(ItemStack stack, World world, ServerPlayerEntity player, BlockPos target) {
        if (player.getMainHandStack().getItem() != this) {
            return;
        }
        BlockState targetState = world.getBlockState(target);
        if (targetState.isAir() || targetState.getHardness(world, target) < 0.0F) {
            return;
        }
        if (this.getMiningSpeedMultiplier(stack, targetState) <= 1.0F) {
            return; // not a block this shovel is good at (stone, ores, ...)
        }
        player.interactionManager.tryBreakBlock(target);
    }
}
