package net.minecraft.client.model;

import net.minecraft.entity.Entity;

public class ModelSlime extends ModelBase {
    /**
     * The slime's bodies, both the inside box and the outside box
     */
    ModelRenderer slimeBodies;

    /**
     * The slime's right eye
     */
    ModelRenderer slimeRightEye;

    /**
     * The slime's left eye
     */
    ModelRenderer slimeLeftEye;

    /**
     * The slime's mouth
     */
    ModelRenderer slimeMouth;

    public ModelSlime(int p_i1157_1_) {
        slimeBodies = new ModelRenderer(this, 0, p_i1157_1_);
        slimeBodies.addBox(-4.0F, 16.0F, -4.0F, 8, 8, 8);

        if (p_i1157_1_ > 0) {
            slimeBodies = new ModelRenderer(this, 0, p_i1157_1_);
            slimeBodies.addBox(-3.0F, 17.0F, -3.0F, 6, 6, 6);
            slimeRightEye = new ModelRenderer(this, 32, 0);
            slimeRightEye.addBox(-3.25F, 18.0F, -3.5F, 2, 2, 2);
            slimeLeftEye = new ModelRenderer(this, 32, 4);
            slimeLeftEye.addBox(1.25F, 18.0F, -3.5F, 2, 2, 2);
            slimeMouth = new ModelRenderer(this, 32, 8);
            slimeMouth.addBox(0.0F, 21.0F, -3.5F, 1, 1, 1);
        }
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
        slimeBodies.render(scale);

        if (slimeRightEye != null) {
            slimeRightEye.render(scale);
            slimeLeftEye.render(scale);
            slimeMouth.render(scale);
        }
    }
}
