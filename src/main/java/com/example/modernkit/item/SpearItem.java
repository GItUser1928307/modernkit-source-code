package com.example.modernkit.item;

import com.example.modernkit.combat.SpearChargeHandler;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Shared implementation for all six spear materials.
 */
public class SpearItem extends Item {

    private final SpearMaterial material;

    private final Multimap<EntityAttribute, EntityAttributeModifier>
            attributeModifiers;

    public SpearItem(SpearMaterial material) {
        super(createSettings(material));

        this.material = material;

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier>
                builder = ImmutableMultimap.builder();

        double damageModifier =
                material.getJabDamage() - 1.0D;

        if (damageModifier != 0.0D) {
            builder.put(
                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                    new EntityAttributeModifier(
                            ATTACK_DAMAGE_MODIFIER_ID,
                            "Weapon modifier",
                            damageModifier,
                            EntityAttributeModifier.Operation.ADDITION
                    )
            );
        }

        builder.put(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(
                        ATTACK_SPEED_MODIFIER_ID,
                        "Weapon modifier",
                        material.getAttackSpeed() - 4.0D,
                        EntityAttributeModifier.Operation.ADDITION
                )
        );

        this.attributeModifiers = builder.build();
    }

    private static Settings createSettings(SpearMaterial material) {
        Settings settings = new Item.Settings()
                .group(ItemGroup.COMBAT)
                .maxDamage(material.getDurability());

        if (material == SpearMaterial.NETHERITE) {
            settings.fireproof();
        }

        return settings;
    }

    /**
     * Material-specific enchantability, using vanilla material values.
     */
    @Override
    public int getEnchantability() {
        switch (material) {
            case WOODEN:
                return 15;
            case STONE:
                return 5;
            case GOLDEN:
                return 22;
            case IRON:
                return 14;
            case DIAMOND:
                return 10;
            case NETHERITE:
                return 15;
            default:
                return 0;
        }
    }

    public SpearMaterial getMaterial() {
        return material;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier>
    getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND
                ? attributeModifiers
                : super.getAttributeModifiers(slot);
    }

    /**
     * Right-click starts a continuous spear charge.
     */
    @Override
    public TypedActionResult<ItemStack> use(
            World world,
            PlayerEntity user,
            Hand hand
    ) {
        user.setCurrentHand(hand);

        return TypedActionResult.consume(
                user.getStackInHand(hand)
        );
    }

    /**
     * Long enough to support extended charge durations.
     */
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    /**
     * Reuse Minecraft's spear/trident holding animation for now.
     * Custom stage-specific animations are a later stage.
     */
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    /**
     * Server-side kinetic collision checks while charging.
     */
    @Override
    public void usageTick(
            World world,
            LivingEntity user,
            ItemStack stack,
            int remainingUseTicks
    ) {

        if (world.isClient || !(user instanceof ServerPlayerEntity)) {
            return;
        }

        int elapsedTicks = getMaxUseTime(stack) - remainingUseTicks;

        SpearChargeHandler.tick(
                (ServerPlayerEntity) user,
                stack,
                elapsedTicks
        );
    }

    /**
     * Clean up contact tracking when the player releases the spear.
     */
    @Override
    public void onStoppedUsing(
            ItemStack stack,
            World world,
            LivingEntity user,
            int remainingUseTicks
    ) {

        if (!world.isClient && user instanceof ServerPlayerEntity) {
            SpearChargeHandler.stop((ServerPlayerEntity) user);
        }
    }

    @Override
    public boolean postHit(
            ItemStack stack,
            LivingEntity target,
            LivingEntity attacker
    ) {
        stack.damage(
                1,
                attacker,
                entity -> entity.sendEquipmentBreakStatus(
                        EquipmentSlot.MAINHAND
                )
        );

        return true;
    }

    @Override
    public boolean canMine(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity miner
    ) {
        return false;
    }
}
