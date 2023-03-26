package net.minecraft.client.renderer.block.statemap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class StateMap extends StateMapperBase {
    private final IProperty<?> name;
    private final String suffix;
    private final List<IProperty<?>> ignored;

    private StateMap(IProperty<?> name, String suffix, List<IProperty<?>> ignored) {
        this.name = name;
        this.suffix = suffix;
        this.ignored = ignored;
    }

    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        Map<IProperty, Comparable> map = Maps.newLinkedHashMap(state.getProperties());
        String s;

        if (name == null) {
            s = Block.blockRegistry.getNameForObject(state.getBlock()).toString();
        } else {
            s = ((IProperty) name).getName(map.remove(name));
        }

        if (suffix != null) {
            s = s + suffix;
        }

        for (IProperty<?> iproperty : ignored) {
            map.remove(iproperty);
        }

        return new ModelResourceLocation(s, getPropertyString(map));
    }

    public static class Builder {
        private final List<IProperty<?>> ignored = Lists.newArrayList();
        private IProperty<?> name;
        private String suffix;

        public StateMap.Builder withName(IProperty<?> builderPropertyIn) {
            name = builderPropertyIn;
            return this;
        }

        public StateMap.Builder withSuffix(String builderSuffixIn) {
            suffix = builderSuffixIn;
            return this;
        }

        public StateMap.Builder ignore(IProperty<?>... p_178442_1_) {
            Collections.addAll(ignored, p_178442_1_);
            return this;
        }

        public StateMap build() {
            return new StateMap(name, suffix, ignored);
        }
    }
}
