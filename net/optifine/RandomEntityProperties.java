package net.optifine;

import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.config.ConnectedParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RandomEntityProperties {
    public String name;
    public String basePath;
    public ResourceLocation[] resourceLocations;
    public RandomEntityRule[] rules;

    public RandomEntityProperties(String path, ResourceLocation[] variants) {
        ConnectedParser connectedparser = new ConnectedParser("RandomEntities");
        name = connectedparser.parseName(path);
        basePath = connectedparser.parseBasePath(path);
        resourceLocations = variants;
    }

    public RandomEntityProperties(Properties props, String path, ResourceLocation baseResLoc) {
        ConnectedParser connectedparser = new ConnectedParser("RandomEntities");
        name = connectedparser.parseName(path);
        basePath = connectedparser.parseBasePath(path);
        rules = parseRules(props, path, baseResLoc, connectedparser);
    }

    public ResourceLocation getTextureLocation(ResourceLocation loc, IRandomEntity randomEntity) {
        if (rules != null) {
            for (RandomEntityRule randomentityrule : rules) {
                if (randomentityrule.matches(randomEntity)) {
                    return randomentityrule.getTextureLocation(loc, randomEntity.getId());
                }
            }
        }

        if (resourceLocations != null) {
            int j = randomEntity.getId();
            int k = j % resourceLocations.length;
            return resourceLocations[k];
        } else {
            return loc;
        }
    }

    private RandomEntityRule[] parseRules(Properties props, String pathProps, ResourceLocation baseResLoc, ConnectedParser cp) {
        List list = new ArrayList();
        int i = props.size();

        for (int j = 0; j < i; ++j) {
            int k = j + 1;
            String s = props.getProperty("textures." + k);

            if (s == null) {
                s = props.getProperty("skins." + k);
            }

            if (s != null) {
                RandomEntityRule randomentityrule = new RandomEntityRule(props, pathProps, baseResLoc, k, s, cp);

                if (randomentityrule.isValid(pathProps)) {
                    list.add(randomentityrule);
                }
            }
        }

        RandomEntityRule[] arandomentityrule = (RandomEntityRule[]) list.toArray(new RandomEntityRule[list.size()]);
        return arandomentityrule;
    }

    public boolean isValid(String path) {
        if (resourceLocations == null && rules == null) {
            Config.warn("No skins specified: " + path);
            return false;
        } else {
            if (rules != null) {
                for (RandomEntityRule randomentityrule : rules) {
                    if (!randomentityrule.isValid(path)) {
                        return false;
                    }
                }
            }

            if (resourceLocations != null) {
                for (ResourceLocation resourcelocation : resourceLocations) {
                    if (!Config.hasResource(resourcelocation)) {
                        Config.warn("Texture not found: " + resourcelocation.getResourcePath());
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public boolean isDefault() {
        return rules == null && resourceLocations == null;
    }
}
