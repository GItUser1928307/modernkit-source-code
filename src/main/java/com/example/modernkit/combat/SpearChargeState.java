package com.example.modernkit.combat;

import com.example.modernkit.item.SpearMaterial;

/**
 * The four possible states of a held spear charge.
 */
public enum SpearChargeState {

    ENGAGED,
    TIRED,
    DISENGAGED,
    IDLE;

    public static SpearChargeState fromElapsedTicks(
            SpearMaterial material,
            int elapsedTicks
    ) {

        if (elapsedTicks < material.getEngagedTicks()) {
            return ENGAGED;
        }

        elapsedTicks -= material.getEngagedTicks();

        if (elapsedTicks < material.getTiredTicks()) {
            return TIRED;
        }

        elapsedTicks -= material.getTiredTicks();

        if (elapsedTicks < material.getDisengagedTicks()) {
            return DISENGAGED;
        }

        return IDLE;
    }
}
