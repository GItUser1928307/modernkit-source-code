package com.example.modernkit.combat;

import com.example.modernkit.item.SpearItem;
import com.example.modernkit.item.SpearMaterial;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import com.example.modernkit.advancement.MobKabobAdvancement;
import net.minecraft.entity.player.PlayerEntity;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles kinetic spear attacks while the player holds right-click.
 *
 * All damage and movement effects are calculated on the server.
 */
public final class SpearChargeHandler {

    private static final double MIN_REACH = 2.0D;
    private static final double MAX_REACH = 4.5D;
    private static final double HITBOX_MARGIN = 0.125D;

    private static final double DAMAGE_SPEED_THRESHOLD = 4.6D;
    private static final double KNOCKBACK_SPEED_THRESHOLD = 5.1D;

    private static final float KNOCKBACK_STRENGTH = 0.6F;

    private static final int CONTACT_COOLDOWN_TICKS = 10;

    private static final Map<UUID, ChargeSession> SESSIONS =
            new HashMap<>();

    private SpearChargeHandler() {
    }

    /**
     * Called every tick while the player actively uses a spear.
     */
    public static void tick(
            ServerPlayerEntity player,
            ItemStack stack,
            int elapsedTicks
    ) {

        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        if (!(stack.getItem() instanceof SpearItem)) {
            return;
        }

        SpearItem spear = (SpearItem) stack.getItem();
        SpearMaterial material = spear.getMaterial();

        // Determine which charge stage is active.
        SpearChargeState state =
                SpearChargeState.fromElapsedTicks(material, elapsedTicks);

        // Completed charge: remove the session.
        if (state == SpearChargeState.IDLE) {
            // Keep this session until the player releases the spear.
            return;
        }

        // Track the current stage and play effects only when it changes.
        ChargeSession session = SESSIONS.computeIfAbsent(
                player.getUuid(),
                uuid -> new ChargeSession()
        );

        if (state != session.lastState) {
            SpearEffects.playChargeStage(player, state);
            session.lastState = state;
        }

        // The weapon cannot hit targets until its activation delay expires.
        if (elapsedTicks < material.getActivationDelayTicks()) {
            return;
        }

        long currentTick = player.world.getTime();

        // Eye position and direction of the player's view.
        Vec3d direction = player.getRotationVec(1.0F).normalize();
        Vec3d origin = player.getCameraPosVec(1.0F);

        Vec3d maximumEnd = origin.add(
                direction.multiply(MAX_REACH)
        );

        // Solid blocks obstruct the charge attack.
        BlockHitResult blockHit = player.world.raycast(
                new RaycastContext(
                        origin,
                        maximumEnd,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                )
        );

        double effectiveReach = MAX_REACH;

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            effectiveReach = Math.min(
                    MAX_REACH,
                    origin.distanceTo(blockHit.getPos())
            );
        }

        if (effectiveReach < MIN_REACH) {
            cleanupContacts(session, currentTick);
            return;
        }

        // Apply the same minimum reach and hitbox expansion as the jab.
        Vec3d rayStart = origin.add(
                direction.multiply(MIN_REACH)
        );

        Vec3d rayEnd = origin.add(
                direction.multiply(effectiveReach)
        );

        Box searchBox = player.getBoundingBox()
                .stretch(
                        direction.x * MAX_REACH,
                        direction.y * MAX_REACH,
                        direction.z * MAX_REACH
                )
                .expand(1.0D);

        List<Entity> candidates = player.world.getOtherEntities(
                player,
                searchBox,
                entity ->
                        entity instanceof LivingEntity
                                && ((LivingEntity) entity).isAlive()
                                && !entity.isSpectator()
                                && !player.isTeammate(entity)
        );

        // Project the attacker's movement along the spear direction.
        double attackerSpeed = player.getVelocity()
                .dotProduct(direction) * 20.0D;

        for (Entity candidate : candidates) {

            LivingEntity target = (LivingEntity) candidate;

            boolean intersectsRay = target.getBoundingBox()
                    .expand(HITBOX_MARGIN)
                    .raycast(rayStart, rayEnd)
                    .isPresent();

            if (!intersectsRay) {
                continue;
            }

            UUID targetId = target.getUuid();

            ContactRecord contact = session.contacts.get(targetId);

            // A target must leave contact for the cooldown to expire
            // before it can be hit by this same charge again.
            if (contact != null
                    && currentTick - contact.lastSeenTick
                    > CONTACT_COOLDOWN_TICKS) {

                session.contacts.remove(targetId);
                contact = null;
            }

            if (contact == null) {
                contact = new ContactRecord();
                session.contacts.put(targetId, contact);
            }

            contact.lastSeenTick = currentTick;

            // One hit per continuous contact.
            if (contact.hitThisContact) {
                continue;
            }

            // Relative velocity projected onto the viewing direction.
            double relativeSpeed = player.getVelocity()
                    .subtract(target.getVelocity())
                    .dotProduct(direction) * 20.0D;

            if (relativeSpeed < DAMAGE_SPEED_THRESHOLD) {
                continue;
            }

            // Vanilla charge formula:
            // 1 base damage + floor(relative speed * material multiplier).
            // Vanilla kinetic charge damage:
            // one base damage + floor(relative speed * material multiplier).
            int kineticDamage = 1 + (int) Math.floor(
                    relativeSpeed * material.getChargeMultiplier()
            );

            // Weapon enchantment bonuses are added after kinetic damage.
            float enchantmentBonus = EnchantmentHelper.getAttackDamage(
                    stack,
                    target.getGroup()
            );

            float finalDamage = kineticDamage + enchantmentBonus;

            boolean damaged = target.damage(
                    DamageSource.player(player),
                    finalDamage
            );

            if (!damaged) {
                continue;
            }

            // Apply vanilla Fire Aspect and Bane hit effects.
            EnchantmentHelper.onTargetDamaged(player, target);


            // Engaged stage: dismount if the attacker meets
            // the material-specific velocity threshold.
            if (state == SpearChargeState.ENGAGED
                    && attackerSpeed >= material.getDismountSpeed()
                    && target.hasVehicle()) {

                target.stopRiding();
            }

            // Engaged and Tired stages can knock targets back.
            if (state != SpearChargeState.DISENGAGED
                    && attackerSpeed >= KNOCKBACK_SPEED_THRESHOLD) {

                double knockbackX = player.getX() - target.getX();
                double knockbackZ = player.getZ() - target.getZ();

                int knockbackLevel = EnchantmentHelper.getLevel(
                        Enchantments.KNOCKBACK,
                        stack
                );

                target.takeKnockback(
                        KNOCKBACK_STRENGTH + knockbackLevel * 0.5F,
                        knockbackX,
                        knockbackZ
                );
            }

            // Charge impact consumes durability.
            stack.damage(
                    1,
                    player,
                    entity -> entity.sendEquipmentBreakStatus(
                            EquipmentSlot.MAINHAND
                    )
            );

            // Stage 6: successful charge impact effects.
            SpearEffects.playChargeImpact(player, target, state);


            // Mob Kabob: count distinct mobs hit during this one use.
            if (!(target instanceof PlayerEntity)
                    && session.hitEntities.add(target.getUuid())
                    && session.hitEntities.size() >= 5
                    && !session.mobKabobAwarded) {

                MobKabobAdvancement.grant(player);
                session.mobKabobAwarded = true;
            }

            contact.hitThisContact = true;
        }

        cleanupContacts(session, currentTick);
    }

    /**
     * Remove stale contact records.
     *
     * A target can be hit again after it has left the attack ray
     * for at least the contact cooldown.
     */
    private static void cleanupContacts(
            ChargeSession session,
            long currentTick
    ) {

        Iterator<Map.Entry<UUID, ContactRecord>> iterator =
                session.contacts.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ContactRecord> entry = iterator.next();

            if (currentTick - entry.getValue().lastSeenTick
                    > CONTACT_COOLDOWN_TICKS) {
                iterator.remove();
            }
        }
    }

    /**
     * Called when the player releases the use button.
     */
    public static void stop(ServerPlayerEntity player) {
        if (player != null) {
            SESSIONS.remove(player.getUuid());
        }
    }

    private static final class ChargeSession {
        private final Map<UUID, ContactRecord> contacts =
                new HashMap<>();

        private final Set<UUID> hitEntities = new HashSet<>();

        private boolean mobKabobAwarded;

        private SpearChargeState lastState;
    }

    private static final class ContactRecord {
        private long lastSeenTick;
        private boolean hitThisContact;
    }
}
