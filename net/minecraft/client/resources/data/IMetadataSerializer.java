package net.minecraft.client.resources.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.util.*;

public class IMetadataSerializer {
    private final IRegistry<String, IMetadataSerializer.Registration<? extends IMetadataSection>> metadataSectionSerializerRegistry = new RegistrySimple();
    private final GsonBuilder gsonBuilder = new GsonBuilder();

    /**
     * Cached Gson instance. Set to null when more sections are registered, and then re-created from the builder.
     */
    private Gson gson;

    public IMetadataSerializer() {
        gsonBuilder.registerTypeHierarchyAdapter(IChatComponent.class, new IChatComponent.Serializer());
        gsonBuilder.registerTypeHierarchyAdapter(ChatStyle.class, new ChatStyle.Serializer());
        gsonBuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
    }

    public <T extends IMetadataSection> void registerMetadataSectionType(IMetadataSectionSerializer<T> metadataSectionSerializer, Class<T> clazz) {
        metadataSectionSerializerRegistry.putObject(metadataSectionSerializer.getSectionName(), new Registration(metadataSectionSerializer, clazz));
        gsonBuilder.registerTypeAdapter(clazz, metadataSectionSerializer);
        gson = null;
    }

    public <T extends IMetadataSection> T parseMetadataSection(String sectionName, JsonObject json) {
        if (sectionName == null) {
            throw new IllegalArgumentException("Metadata section name cannot be null");
        } else if (!json.has(sectionName)) {
            return null;
        } else if (!json.get(sectionName).isJsonObject()) {
            throw new IllegalArgumentException("Invalid metadata for '" + sectionName + "' - expected object, found " + json.get(sectionName));
        } else {
            IMetadataSerializer.Registration<?> registration = metadataSectionSerializerRegistry.getObject(sectionName);

            if (registration == null) {
                throw new IllegalArgumentException("Don't know how to handle metadata section '" + sectionName + "'");
            } else {
                return (T) getGson().fromJson(json.getAsJsonObject(sectionName), registration.clazz);
            }
        }
    }

    /**
     * Returns a Gson instance with type adapters registered for metadata sections.
     */
    private Gson getGson() {
        if (gson == null) {
            gson = gsonBuilder.create();
        }

        return gson;
    }

    static class Registration<T extends IMetadataSection> {
        final IMetadataSectionSerializer<T> section;
        final Class<T> clazz;

        private Registration(IMetadataSectionSerializer<T> metadataSectionSerializer, Class<T> clazzToRegister) {
            section = metadataSectionSerializer;
            clazz = clazzToRegister;
        }
    }
}
