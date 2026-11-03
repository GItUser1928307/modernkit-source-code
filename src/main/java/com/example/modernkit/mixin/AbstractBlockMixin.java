package com.example.modernkit.mixin;

import com.example.modernkit.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
    @Inject(method = "getDroppedStacks", at = @At("HEAD"), cancellable = true)
    private void modernkit$dropBedrock(BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        ItemStack tool = builder.getNullable(LootContextParameters.TOOL);
        if (state.isOf(Blocks.BEDROCK) && tool != null && tool.getItem() == ModItems.BEDROCK_PICKAXE) {
            cir.setReturnValue(Collections.singletonList(new ItemStack(Items.BEDROCK)));
        }
    }
}
