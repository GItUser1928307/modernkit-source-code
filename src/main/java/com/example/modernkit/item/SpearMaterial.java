package com.example.modernkit.item;

public enum SpearMaterial {

    WOODEN(59, 1.0F, 1.5384616F,
            0.70F, 15, 100, 100, 100, 14.0F),

    STONE(131, 2.0F, 1.3333334F,
            0.82F, 14, 90, 90, 95, 13.0F),

    GOLDEN(32, 1.0F, 1.0526316F,
            0.70F, 14, 70, 100, 105, 13.0F),

    IRON(250, 3.0F, 1.0526316F,
            0.95F, 12, 50, 85, 90, 11.0F),

    DIAMOND(1561, 4.0F, 0.9523810F,
            1.075F, 10, 60, 70, 70, 10.0F),

    NETHERITE(2031, 5.0F, 0.86956525F,
            1.20F, 8, 50, 60, 65, 9.0F);

    private final int durability;
    private final float jabDamage;
    private final float attackSpeed;

    private final float chargeMultiplier;
    private final int activationDelayTicks;

    private final int engagedTicks;
    private final int tiredTicks;
    private final int disengagedTicks;

    private final float dismountSpeed;

    SpearMaterial(
            int durability,
            float jabDamage,
            float attackSpeed,
            float chargeMultiplier,
            int activationDelayTicks,
            int engagedTicks,
            int tiredTicks,
            int disengagedTicks,
            float dismountSpeed
    ) {
        this.durability = durability;
        this.jabDamage = jabDamage;
        this.attackSpeed = attackSpeed;
        this.chargeMultiplier = chargeMultiplier;
        this.activationDelayTicks = activationDelayTicks;
        this.engagedTicks = engagedTicks;
        this.tiredTicks = tiredTicks;
        this.disengagedTicks = disengagedTicks;
        this.dismountSpeed = dismountSpeed;
    }

    public int getDurability() {
        return durability;
    }

    public float getJabDamage() {
        return jabDamage;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public float getChargeMultiplier() {
        return chargeMultiplier;
    }

    public int getActivationDelayTicks() {
        return activationDelayTicks;
    }

    public int getEngagedTicks() {
        return engagedTicks;
    }

    public int getTiredTicks() {
        return tiredTicks;
    }

    public int getDisengagedTicks() {
        return disengagedTicks;
    }

    public float getDismountSpeed() {
        return dismountSpeed;
    }

    public int getTotalChargeTicks() {
        return engagedTicks + tiredTicks + disengagedTicks;
    }
}
