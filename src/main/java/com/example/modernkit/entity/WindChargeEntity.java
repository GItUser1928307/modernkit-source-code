package com.example.modernkit.entity;

import com.example.modernkit.item.ModItems;
import com.example.modernkit.sound.ModSounds;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class WindChargeEntity extends ThrownItemEntity {

    private static final double BURST_RADIUS = 2.4D;

    public WindChargeEntity(
            EntityType<? extends WindChargeEntity> type,
            World world
    ) {
        super(type, world);
    }

    public WindChargeEntity(World world, LivingEntity owner) {
        super(ModEntities.WIND_CHARGE, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.WIND_CHARGE;
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (world.isClient) {
            return true;
        }

        Entity deflector = source.getAttacker();
        if (deflector == null) {
            return false;
        }

        Vec3d direction = deflector.getRotationVec(1.0F);
        Entity sourceEntity = source.getSource();
        if (sourceEntity instanceof ProjectileEntity) {
            Vec3d projectileVelocity = sourceEntity.getVelocity();
            if (projectileVelocity.lengthSquared() > 1.0E-6D) {
                direction = projectileVelocity.normalize();
            }
        }

        if (direction.lengthSquared() < 1.0E-6D) {
            return false;
        }

        setOwner(deflector);
        setVelocity(direction.normalize().multiply(1.5D));
        velocityModified = true;
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        if (!world.isClient) {
            Entity target = hit.getEntity();
            target.damage(
                    DamageSource.thrownProjectile(this, getOwner()),
                    1.0F
            );
            burst(target.getX(), target.getBodyY(0.5D), target.getZ());
        }

        remove();
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        if (!world.isClient) {
            Vec3d impact = hit.getPos();
            burst(impact.x, impact.y, impact.z);
        }

        remove();
    }

    private void burst(double x, double y, double z) {
        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.playSound(
                null,
                x,
                y,
                z,
                ModSounds.WIND_CHARGE_BURST,
                SoundCategory.PLAYERS,
                0.8F,
                0.9F + random.nextFloat() * 0.2F
        );
        serverWorld.spawnParticles(
                ParticleTypes.CLOUD,
                x,
                y,
                z,
                24,
                0.45D,
                0.35D,
                0.45D,
                0.12D
        );
        serverWorld.spawnParticles(
                ParticleTypes.POOF,
                x,
                y,
                z,
                8,
                0.25D,
                0.25D,
                0.25D,
                0.04D
        );

        interactWithBlocks(x, y, z);

        Box area = new Box(
                x - BURST_RADIUS,
                y - BURST_RADIUS,
                z - BURST_RADIUS,
                x + BURST_RADIUS,
                y + BURST_RADIUS,
                z + BURST_RADIUS
        );
        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
        );

        for (LivingEntity entity : entities) {
            if (!entity.isPushable()) {
                continue;
            }

            double dx = entity.getX() - x;
            double dy = entity.getBodyY(0.5D) - y;
            double dz = entity.getZ() - z;
            double distanceSquared = dx * dx + dy * dy + dz * dz;

            if (distanceSquared >= BURST_RADIUS * BURST_RADIUS) {
                continue;
            }

            double distance = Math.sqrt(distanceSquared);
            double strength = 1.4D * (1.0D - distance / BURST_RADIUS);
            int blastProtection = EnchantmentHelper.getEquipmentLevel(
                    Enchantments.BLAST_PROTECTION,
                    entity
            );
            strength *= 1.0D - Math.min(20, blastProtection * 2) / 25.0D;
            double knockbackResistance = Math.max(
                    0.0D,
                    Math.min(
                            1.0D,
                            entity.getAttributeValue(
                                    EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE
                            )
                    )
            );
            strength *= 1.0D - knockbackResistance;

            if (strength <= 0.0D) {
                continue;
            }

            double horizontalLength = Math.sqrt(dx * dx + dz * dz);
            double horizontalScale =
                    horizontalLength < 1.0E-6D
                            ? 0.0D
                            : strength / horizontalLength;
            double vertical = 0.45D + strength * 0.65D;

            entity.setVelocity(
                    entity.getVelocity().add(
                            dx * horizontalScale,
                            vertical,
                            dz * horizontalScale
                    )
            );
            entity.velocityModified = true;

            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.networkHandler.sendPacket(
                        new EntityVelocityUpdateS2CPacket(player)
                );
            }
        }
    }

    private void interactWithBlocks(double x, double y, double z) {
        int minX = MathHelper.floor(x - BURST_RADIUS);
        int minY = MathHelper.floor(y - BURST_RADIUS);
        int minZ = MathHelper.floor(z - BURST_RADIUS);
        int maxX = MathHelper.floor(x + BURST_RADIUS);
        int maxY = MathHelper.floor(y + BURST_RADIUS);
        int maxZ = MathHelper.floor(z + BURST_RADIUS);

        for (int blockX = minX; blockX <= maxX; blockX++) {
            for (int blockY = minY; blockY <= maxY; blockY++) {
                for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                    double dx = blockX + 0.5D - x;
                    double dy = blockY + 0.5D - y;
                    double dz = blockZ + 0.5D - z;

                    if (dx * dx + dy * dy + dz * dz
                            <= BURST_RADIUS * BURST_RADIUS) {
                        interactWithBlock(new BlockPos(blockX, blockY, blockZ));
                    }
                }
            }
        }
    }

    private void interactWithBlock(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof AbstractButtonBlock) {
            AbstractButtonBlock button = (AbstractButtonBlock) block;
            if (!state.get(AbstractButtonBlock.POWERED)) {
                button.powerOn(state, world, pos);
            }
            return;
        }

        if (block instanceof LeverBlock) {
            world.setBlockState(
                    pos,
                    state.with(
                            LeverBlock.POWERED,
                            !state.get(LeverBlock.POWERED)
                    ),
                    3
            );
            world.playSound(
                    null,
                    pos,
                    SoundEvents.BLOCK_LEVER_CLICK,
                    SoundCategory.BLOCKS,
                    0.5F,
                    0.6F
            );
            return;
        }

        if (block instanceof DoorBlock && block != Blocks.IRON_DOOR) {
            if (state.get(DoorBlock.HALF)
                    == DoubleBlockHalf.UPPER) {
                return;
            }

            DoorBlock door = (DoorBlock) block;
            door.setOpen(
                    world,
                    state,
                    pos,
                    !state.get(DoorBlock.OPEN)
            );
            return;
        }

        if (block instanceof TrapdoorBlock && block != Blocks.IRON_TRAPDOOR) {
            boolean open = !state.get(TrapdoorBlock.OPEN);
            world.setBlockState(pos, state.with(TrapdoorBlock.OPEN, open), 3);
            world.playSound(
                    null,
                    pos,
                    open
                            ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN
                            : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE,
                    SoundCategory.BLOCKS,
                    1.0F,
                    0.9F
            );
            return;
        }

        if (block instanceof FenceGateBlock) {
            boolean open = !state.get(FenceGateBlock.OPEN);
            world.setBlockState(pos, state.with(FenceGateBlock.OPEN, open), 3);
            world.playSound(
                    null,
                    pos,
                    open
                            ? SoundEvents.BLOCK_FENCE_GATE_OPEN
                            : SoundEvents.BLOCK_FENCE_GATE_CLOSE,
                    SoundCategory.BLOCKS,
                    1.0F,
                    0.9F
            );
            return;
        }

        if (block instanceof BellBlock) {
            ((BellBlock) block).ring(world, pos, Direction.UP);
            return;
        }

        if (block instanceof ChorusFlowerBlock) {
            world.breakBlock(pos, true, getOwner(), 512);
        }
    }
}
