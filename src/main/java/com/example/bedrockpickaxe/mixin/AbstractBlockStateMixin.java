package com.example.bedrockpickaxe.mixin;

import com.example.bedrockpickaxe.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
    // Pretend bedrock has this hardness when mined with the Bedrock Pickaxe (obsidian is 50).
    private static final float BEDROCK_HARDNESS = 25.0F;

    @Inject(method = "calcBlockBreakingDelta", at = @At("HEAD"), cancellable = true)
    private void bedrockpickaxe$mineBedrock(PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        BlockState state = (BlockState) (Object) this;
        if (state.isOf(Blocks.BEDROCK) && player.getMainHandStack().getItem() == ModItems.BEDROCK_PICKAXE) {
            cir.setReturnValue(player.getBlockBreakingSpeed(state) / BEDROCK_HARDNESS / 30.0F);
        }
    }
}
