package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelWitch extends ModelVillager {
    private final ModelRenderer field_82901_h = (new ModelRenderer(this)).setTextureSize(64, 128);
    private final ModelRenderer witchHat;
    public boolean field_82900_g;

    public ModelWitch(float p_i46361_1_) {
        super(p_i46361_1_, 0.0F, 64, 128);
        field_82901_h.setRotationPoint(0.0F, -2.0F, 0.0F);
        field_82901_h.setTextureOffset(0, 0).addBox(0.0F, 3.0F, -6.75F, 1, 1, 1, -0.25F);
        villagerNose.addChild(field_82901_h);
        witchHat = (new ModelRenderer(this)).setTextureSize(64, 128);
        witchHat.setRotationPoint(-5.0F, -10.03125F, -5.0F);
        witchHat.setTextureOffset(0, 64).addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
        villagerHead.addChild(witchHat);
        ModelRenderer modelrenderer = (new ModelRenderer(this)).setTextureSize(64, 128);
        modelrenderer.setRotationPoint(1.75F, -4.0F, 2.0F);
        modelrenderer.setTextureOffset(0, 76).addBox(0.0F, 0.0F, 0.0F, 7, 4, 7);
        modelrenderer.rotateAngleX = -0.05235988F;
        modelrenderer.rotateAngleZ = 0.02617994F;
        witchHat.addChild(modelrenderer);
        ModelRenderer modelrenderer1 = (new ModelRenderer(this)).setTextureSize(64, 128);
        modelrenderer1.setRotationPoint(1.75F, -4.0F, 2.0F);
        modelrenderer1.setTextureOffset(0, 87).addBox(0.0F, 0.0F, 0.0F, 4, 4, 4);
        modelrenderer1.rotateAngleX = -0.10471976F;
        modelrenderer1.rotateAngleZ = 0.05235988F;
        modelrenderer.addChild(modelrenderer1);
        ModelRenderer modelrenderer2 = (new ModelRenderer(this)).setTextureSize(64, 128);
        modelrenderer2.setRotationPoint(1.75F, -2.0F, 2.0F);
        modelrenderer2.setTextureOffset(0, 95).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
        modelrenderer2.rotateAngleX = -0.20943952F;
        modelrenderer2.rotateAngleZ = 0.10471976F;
        modelrenderer1.addChild(modelrenderer2);
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        villagerNose.offsetX = villagerNose.offsetY = villagerNose.offsetZ = 0.0F;
        float f = 0.01F * (float) (entityIn.getEntityId() % 10);
        villagerNose.rotateAngleX = MathHelper.sin((float) entityIn.ticksExisted * f) * 4.5F * (float) Math.PI / 180.0F;
        villagerNose.rotateAngleY = 0.0F;
        villagerNose.rotateAngleZ = MathHelper.cos((float) entityIn.ticksExisted * f) * 2.5F * (float) Math.PI / 180.0F;

        if (field_82900_g) {
            villagerNose.rotateAngleX = -0.9F;
            villagerNose.offsetZ = -0.09375F;
            villagerNose.offsetY = 0.1875F;
        }
    }
}
