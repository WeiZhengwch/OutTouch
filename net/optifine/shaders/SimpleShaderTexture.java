package net.optifine.shaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.*;
import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class SimpleShaderTexture extends AbstractTexture {
    private static final IMetadataSerializer METADATA_SERIALIZER = makeMetadataSerializer();
    private final String texturePath;

    public SimpleShaderTexture(String texturePath) {
        this.texturePath = texturePath;
    }

    public static TextureMetadataSection loadTextureMetadataSection(String texturePath, TextureMetadataSection def) {
        String s = texturePath + ".mcmeta";
        String s1 = "texture";
        InputStream inputstream = Shaders.getShaderPackResourceStream(s);

        if (inputstream != null) {
            IMetadataSerializer imetadataserializer = METADATA_SERIALIZER;
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
            TextureMetadataSection texturemetadatasection1;

            try {
                JsonObject jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
                TextureMetadataSection texturemetadatasection = imetadataserializer.parseMetadataSection(s1, jsonobject);

                if (texturemetadatasection == null) {
                    return def;
                }

                texturemetadatasection1 = texturemetadatasection;
            } catch (RuntimeException runtimeexception) {
                SMCLog.warning("Error reading metadata: " + s);
                SMCLog.warning(runtimeexception.getClass().getName() + ": " + runtimeexception.getMessage());
                return def;
            } finally {
                IOUtils.closeQuietly(bufferedreader);
                IOUtils.closeQuietly(inputstream);
            }

            return texturemetadatasection1;
        } else {
            return def;
        }
    }

    private static IMetadataSerializer makeMetadataSerializer() {
        IMetadataSerializer imetadataserializer = new IMetadataSerializer();
        imetadataserializer.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
        imetadataserializer.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
        imetadataserializer.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
        imetadataserializer.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
        imetadataserializer.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
        return imetadataserializer;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        deleteGlTexture();
        InputStream inputstream = Shaders.getShaderPackResourceStream(texturePath);

        if (inputstream == null) {
            throw new FileNotFoundException("Shader texture not found: " + texturePath);
        } else {
            try {
                BufferedImage bufferedimage = TextureUtil.readBufferedImage(inputstream);
                TextureMetadataSection texturemetadatasection = loadTextureMetadataSection(texturePath, new TextureMetadataSection(false, false, new ArrayList()));
                TextureUtil.uploadTextureImageAllocate(getGlTextureId(), bufferedimage, texturemetadatasection.getTextureBlur(), texturemetadatasection.getTextureClamp());
            } finally {
                IOUtils.closeQuietly(inputstream);
            }
        }
    }
}
