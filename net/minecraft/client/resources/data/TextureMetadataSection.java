package net.minecraft.client.resources.data;

import java.util.Collections;
import java.util.List;

public class TextureMetadataSection implements IMetadataSection {
    private final boolean textureBlur;
    private final boolean textureClamp;
    private final List<Integer> listMipmaps;

    public TextureMetadataSection(boolean p_i45102_1_, boolean p_i45102_2_, List<Integer> p_i45102_3_) {
        textureBlur = p_i45102_1_;
        textureClamp = p_i45102_2_;
        listMipmaps = p_i45102_3_;
    }

    public boolean getTextureBlur() {
        return textureBlur;
    }

    public boolean getTextureClamp() {
        return textureClamp;
    }

    public List<Integer> getListMipmaps() {
        return Collections.unmodifiableList(listMipmaps);
    }
}
