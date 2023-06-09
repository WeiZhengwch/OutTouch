package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.Objects;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuView;
import net.minecraft.client.gui.spectator.SpectatorMenu;

import java.util.List;

public class SpectatorDetails {
    private final ISpectatorMenuView field_178684_a;
    private final List<ISpectatorMenuObject> field_178682_b;
    private final int field_178683_c;

    public SpectatorDetails(ISpectatorMenuView p_i45494_1_, List<ISpectatorMenuObject> p_i45494_2_, int p_i45494_3_) {
        field_178684_a = p_i45494_1_;
        field_178682_b = p_i45494_2_;
        field_178683_c = p_i45494_3_;
    }

    public ISpectatorMenuObject func_178680_a(int p_178680_1_) {
        return p_178680_1_ >= 0 && p_178680_1_ < field_178682_b.size() ? Objects.firstNonNull(field_178682_b.get(p_178680_1_), SpectatorMenu.field_178657_a) : SpectatorMenu.field_178657_a;
    }

    public int func_178681_b() {
        return field_178683_c;
    }
}
