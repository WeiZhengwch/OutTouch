package net.minecraft.client.renderer.block.model;

import com.google.gson.*;
import net.minecraft.client.renderer.GlStateManager;

import java.lang.reflect.Type;

public class ItemCameraTransforms {
    public static final ItemCameraTransforms DEFAULT = new ItemCameraTransforms();
    public static float field_181690_b;
    public static float field_181691_c;
    public static float field_181692_d;
    public static float field_181693_e;
    public static float field_181694_f;
    public static float field_181695_g;
    public static float field_181696_h;
    public static float field_181697_i;
    public static float field_181698_j;
    public final ItemTransformVec3f thirdPerson;
    public final ItemTransformVec3f firstPerson;
    public final ItemTransformVec3f head;
    public final ItemTransformVec3f gui;
    public final ItemTransformVec3f ground;
    public final ItemTransformVec3f fixed;

    private ItemCameraTransforms() {
        this(ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT);
    }

    public ItemCameraTransforms(ItemCameraTransforms transforms) {
        thirdPerson = transforms.thirdPerson;
        firstPerson = transforms.firstPerson;
        head = transforms.head;
        gui = transforms.gui;
        ground = transforms.ground;
        fixed = transforms.fixed;
    }

    public ItemCameraTransforms(ItemTransformVec3f thirdPersonIn, ItemTransformVec3f firstPersonIn, ItemTransformVec3f headIn, ItemTransformVec3f guiIn, ItemTransformVec3f groundIn, ItemTransformVec3f fixedIn) {
        thirdPerson = thirdPersonIn;
        firstPerson = firstPersonIn;
        head = headIn;
        gui = guiIn;
        ground = groundIn;
        fixed = fixedIn;
    }

    public void applyTransform(ItemCameraTransforms.TransformType type) {
        ItemTransformVec3f itemtransformvec3f = getTransform(type);

        if (itemtransformvec3f != ItemTransformVec3f.DEFAULT) {
            GlStateManager.translate(itemtransformvec3f.translation.x + field_181690_b, itemtransformvec3f.translation.y + field_181691_c, itemtransformvec3f.translation.z + field_181692_d);
            GlStateManager.rotate(itemtransformvec3f.rotation.y + field_181694_f, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(itemtransformvec3f.rotation.x + field_181693_e, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(itemtransformvec3f.rotation.z + field_181695_g, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(itemtransformvec3f.scale.x + field_181696_h, itemtransformvec3f.scale.y + field_181697_i, itemtransformvec3f.scale.z + field_181698_j);
        }
    }

    public ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType type) {
        return switch (type) {
            case THIRD_PERSON -> thirdPerson;
            case FIRST_PERSON -> firstPerson;
            case HEAD -> head;
            case GUI -> gui;
            case GROUND -> ground;
            case FIXED -> fixed;
            default -> ItemTransformVec3f.DEFAULT;
        };
    }

    public boolean func_181687_c(ItemCameraTransforms.TransformType type) {
        return !getTransform(type).equals(ItemTransformVec3f.DEFAULT);
    }

    public enum TransformType {
        NONE,
        THIRD_PERSON,
        FIRST_PERSON,
        HEAD,
        GUI,
        GROUND,
        FIXED
    }

    static class Deserializer implements JsonDeserializer<ItemCameraTransforms> {
        public ItemCameraTransforms deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
            ItemTransformVec3f itemtransformvec3f = func_181683_a(p_deserialize_3_, jsonobject, "thirdperson");
            ItemTransformVec3f itemtransformvec3f1 = func_181683_a(p_deserialize_3_, jsonobject, "firstperson");
            ItemTransformVec3f itemtransformvec3f2 = func_181683_a(p_deserialize_3_, jsonobject, "head");
            ItemTransformVec3f itemtransformvec3f3 = func_181683_a(p_deserialize_3_, jsonobject, "gui");
            ItemTransformVec3f itemtransformvec3f4 = func_181683_a(p_deserialize_3_, jsonobject, "ground");
            ItemTransformVec3f itemtransformvec3f5 = func_181683_a(p_deserialize_3_, jsonobject, "fixed");
            return new ItemCameraTransforms(itemtransformvec3f, itemtransformvec3f1, itemtransformvec3f2, itemtransformvec3f3, itemtransformvec3f4, itemtransformvec3f5);
        }

        private ItemTransformVec3f func_181683_a(JsonDeserializationContext p_181683_1_, JsonObject p_181683_2_, String p_181683_3_) {
            return p_181683_2_.has(p_181683_3_) ? (ItemTransformVec3f) p_181683_1_.deserialize(p_181683_2_.get(p_181683_3_), ItemTransformVec3f.class) : ItemTransformVec3f.DEFAULT;
        }
    }
}
