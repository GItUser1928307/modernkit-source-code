package com.example.modernkit.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

/**
 * Emerald armor with real 3D pieces attached to the normal biped parts.
 * "outer" (scale 1.0) is used for helmet, chestplate and boots; "inner" (scale 0.5) for leggings.
 * UV offsets must match scripts/make_emerald_3d_textures.py (texture is 64x64).
 */
@Environment(EnvType.CLIENT)
public class EmeraldArmorModel extends BipedEntityModel<LivingEntity> {

    public EmeraldArmorModel(float scale, boolean inner) {
        super(scale, 0.0F, 64, 64);

        if (inner) {
            // leggings: belt buckle, knee plates, thigh plates
            box(body, 24, 32, -3.0F, 7.0F, -3.5F, 6, 3, 1);
            box(rightLeg, 0, 32, -2.0F, 4.5F, -3.5F, 4, 3, 1);
            box(rightLeg, 12, 32, -3.5F, 1.0F, -2.0F, 1, 5, 4);
            box(leftLeg, 0, 32, -2.0F, 4.5F, -3.5F, 4, 3, 1);
            box(leftLeg, 12, 32, 2.5F, 1.0F, -2.0F, 1, 5, 4);
        } else {
            // helmet: crest fin and cheek guards
            box(head, 0, 32, -0.5F, -12.0F, -4.0F, 1, 3, 8);
            box(head, 18, 32, -6.0F, -4.0F, -2.5F, 1, 4, 5);
            box(head, 18, 32, 5.0F, -4.0F, -2.5F, 1, 4, 5);

            // chestplate: chest plate, abdomen plate, back plate
            box(body, 30, 32, -4.0F, 1.0F, -4.0F, 8, 5, 1);
            box(body, 30, 38, -3.0F, 7.0F, -3.75F, 6, 3, 1);
            box(body, 0, 43, -3.0F, 1.0F, 3.0F, 6, 8, 1);

            // shoulder pauldrons (cap on top + side plate on the outside)
            box(rightArm, 14, 43, -4.0F, -5.0F, -3.0F, 6, 2, 6);
            box(rightArm, 38, 42, -5.0F, -3.0F, -3.0F, 1, 4, 6);
            box(leftArm, 14, 43, -2.0F, -5.0F, -3.0F, 6, 2, 6);
            box(leftArm, 38, 42, 4.0F, -3.0F, -3.0F, 1, 4, 6);

            // boots: cuff ring and toe cap
            box(rightLeg, 0, 52, -3.5F, 7.5F, -3.5F, 7, 2, 7);
            box(rightLeg, 28, 52, -3.0F, 10.5F, -4.5F, 6, 2, 2);
            box(leftLeg, 0, 52, -3.5F, 7.5F, -3.5F, 7, 2, 7);
            box(leftLeg, 28, 52, -3.0F, 10.5F, -4.5F, 6, 2, 2);
        }
    }

    private void box(ModelPart parent, int u, int v, float x, float y, float z, int w, int h, int d) {
        ModelPart piece = new ModelPart(this, u, v);
        piece.addCuboid(x, y, z, (float) w, (float) h, (float) d);
        parent.addChild(piece);
    }
}
