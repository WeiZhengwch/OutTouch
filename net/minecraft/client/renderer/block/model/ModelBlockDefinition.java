package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("ALL")
public class ModelBlockDefinition {
    static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ModelBlockDefinition.class, new ModelBlockDefinition.Deserializer()).registerTypeAdapter(ModelBlockDefinition.Variant.class, new ModelBlockDefinition.Variant.Deserializer()).create();
    private final Map<String, ModelBlockDefinition.Variants> mapVariants = Maps.newHashMap();

    public ModelBlockDefinition(Collection<ModelBlockDefinition.Variants> p_i46221_1_) {
        for (ModelBlockDefinition.Variants modelblockdefinition$variants : p_i46221_1_) {
            mapVariants.put(modelblockdefinition$variants.name, modelblockdefinition$variants);
        }
    }

    public ModelBlockDefinition(List<ModelBlockDefinition> p_i46222_1_) {
        for (ModelBlockDefinition modelblockdefinition : p_i46222_1_) {
            mapVariants.putAll(modelblockdefinition.mapVariants);
        }
    }

    public static ModelBlockDefinition parseFromReader(Reader p_178331_0_) {
        return GSON.fromJson(p_178331_0_, ModelBlockDefinition.class);
    }

    public ModelBlockDefinition.Variants getVariants(String p_178330_1_) {
        ModelBlockDefinition.Variants modelblockdefinition$variants = mapVariants.get(p_178330_1_);

        if (modelblockdefinition$variants == null) {
            throw new ModelBlockDefinition.MissingVariantException();
        } else {
            return modelblockdefinition$variants;
        }
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ instanceof ModelBlockDefinition modelblockdefinition) {
            return mapVariants.equals(modelblockdefinition.mapVariants);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return mapVariants.hashCode();
    }

    public static class Deserializer implements JsonDeserializer<ModelBlockDefinition> {
        public ModelBlockDefinition deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
            List<ModelBlockDefinition.Variants> list = parseVariantsList(p_deserialize_3_, jsonobject);
            return new ModelBlockDefinition(list);
        }

        protected List<ModelBlockDefinition.Variants> parseVariantsList(JsonDeserializationContext p_178334_1_, JsonObject p_178334_2_) {
            JsonObject jsonobject = JsonUtils.getJsonObject(p_178334_2_, "variants");
            List<ModelBlockDefinition.Variants> list = Lists.newArrayList();

            for (Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                list.add(parseVariants(p_178334_1_, entry));
            }

            return list;
        }

        protected ModelBlockDefinition.Variants parseVariants(JsonDeserializationContext p_178335_1_, Entry<String, JsonElement> p_178335_2_) {
            String s = p_178335_2_.getKey();
            List<ModelBlockDefinition.Variant> list = Lists.newArrayList();
            JsonElement jsonelement = p_178335_2_.getValue();

            if (jsonelement.isJsonArray()) {
                for (JsonElement jsonelement1 : jsonelement.getAsJsonArray()) {
                    list.add(p_178335_1_.deserialize(jsonelement1, Variant.class));
                }
            } else {
                list.add(p_178335_1_.deserialize(jsonelement, Variant.class));
            }

            return new ModelBlockDefinition.Variants(s, list);
        }
    }

    public static class Variant {
        private final ResourceLocation modelLocation;
        private final ModelRotation modelRotation;
        private final boolean uvLock;
        private final int weight;

        public Variant(ResourceLocation modelLocationIn, ModelRotation modelRotationIn, boolean uvLockIn, int weightIn) {
            modelLocation = modelLocationIn;
            modelRotation = modelRotationIn;
            uvLock = uvLockIn;
            weight = weightIn;
        }

        public ResourceLocation getModelLocation() {
            return modelLocation;
        }

        public ModelRotation getRotation() {
            return modelRotation;
        }

        public boolean isUvLocked() {
            return uvLock;
        }

        public int getWeight() {
            return weight;
        }

        public boolean equals(Object p_equals_1_) {
            if (this == p_equals_1_) {
                return true;
            } else if (!(p_equals_1_ instanceof Variant modelblockdefinition$variant)) {
                return false;
            } else {
                return modelLocation.equals(modelblockdefinition$variant.modelLocation) && modelRotation == modelblockdefinition$variant.modelRotation && uvLock == modelblockdefinition$variant.uvLock;
            }
        }

        public int hashCode() {
            int i = modelLocation.hashCode();
            i = 31 * i + (modelRotation != null ? modelRotation.hashCode() : 0);
            i = 31 * i + (uvLock ? 1 : 0);
            return i;
        }

        public static class Deserializer implements JsonDeserializer<ModelBlockDefinition.Variant> {
            public ModelBlockDefinition.Variant deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
                JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
                String s = parseModel(jsonobject);
                ModelRotation modelrotation = parseRotation(jsonobject);
                boolean flag = parseUvLock(jsonobject);
                int i = parseWeight(jsonobject);
                return new ModelBlockDefinition.Variant(makeModelLocation(s), modelrotation, flag, i);
            }

            private ResourceLocation makeModelLocation(String p_178426_1_) {
                ResourceLocation resourcelocation = new ResourceLocation(p_178426_1_);
                resourcelocation = new ResourceLocation(resourcelocation.getResourceDomain(), "block/" + resourcelocation.getResourcePath());
                return resourcelocation;
            }

            private boolean parseUvLock(JsonObject p_178429_1_) {
                return JsonUtils.getBoolean(p_178429_1_, "uvlock", false);
            }

            protected ModelRotation parseRotation(JsonObject p_178428_1_) {
                int i = JsonUtils.getInt(p_178428_1_, "x", 0);
                int j = JsonUtils.getInt(p_178428_1_, "y", 0);
                ModelRotation modelrotation = ModelRotation.getModelRotation(i, j);

                if (modelrotation == null) {
                    throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
                } else {
                    return modelrotation;
                }
            }

            protected String parseModel(JsonObject p_178424_1_) {
                return JsonUtils.getString(p_178424_1_, "model");
            }

            protected int parseWeight(JsonObject p_178427_1_) {
                return JsonUtils.getInt(p_178427_1_, "weight", 1);
            }
        }
    }

    public static class Variants {
        private final String name;
        private final List<ModelBlockDefinition.Variant> listVariants;

        public Variants(String nameIn, List<ModelBlockDefinition.Variant> listVariantsIn) {
            name = nameIn;
            listVariants = listVariantsIn;
        }

        public List<ModelBlockDefinition.Variant> getVariants() {
            return listVariants;
        }

        public boolean equals(Object p_equals_1_) {
            if (this == p_equals_1_) {
                return true;
            } else if (!(p_equals_1_ instanceof Variants modelblockdefinition$variants)) {
                return false;
            } else {
                return name.equals(modelblockdefinition$variants.name) && listVariants.equals(modelblockdefinition$variants.listVariants);
            }
        }

        public int hashCode() {
            int i = name.hashCode();
            i = 31 * i + listVariants.hashCode();
            return i;
        }
    }

    public class MissingVariantException extends RuntimeException {
    }
}
