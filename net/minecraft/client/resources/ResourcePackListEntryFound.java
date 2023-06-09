package net.minecraft.client.resources;

import net.minecraft.client.gui.GuiScreenResourcePacks;

public class ResourcePackListEntryFound extends ResourcePackListEntry {
    private final ResourcePackRepository.Entry field_148319_c;

    public ResourcePackListEntryFound(GuiScreenResourcePacks resourcePacksGUIIn, ResourcePackRepository.Entry p_i45053_2_) {
        super(resourcePacksGUIIn);
        field_148319_c = p_i45053_2_;
    }

    protected void func_148313_c() {
        field_148319_c.bindTexturePackIcon(mc.getTextureManager());
    }

    protected int func_183019_a() {
        return field_148319_c.func_183027_f();
    }

    protected String func_148311_a() {
        return field_148319_c.getTexturePackDescription();
    }

    protected String func_148312_b() {
        return field_148319_c.getResourcePackName();
    }

    public ResourcePackRepository.Entry func_148318_i() {
        return field_148319_c;
    }
}
