package net.optifine.shaders.uniform;

import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.expr.ConstantFloat;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionResolver;
import net.optifine.shaders.SMCLog;

import java.util.HashMap;
import java.util.Map;

public class ShaderExpressionResolver implements IExpressionResolver {
    private final Map<String, IExpression> mapExpressions = new HashMap();

    public ShaderExpressionResolver(Map<String, IExpression> map) {
        registerExpressions();

        for (String s : map.keySet()) {
            IExpression iexpression = map.get(s);
            registerExpression(s, iexpression);
        }
    }

    private void registerExpressions() {
        ShaderParameterFloat[] ashaderparameterfloat = ShaderParameterFloat.values();

        for (ShaderParameterFloat shaderparameterfloat : ashaderparameterfloat) {
            addParameterFloat(mapExpressions, shaderparameterfloat);
        }

        ShaderParameterBool[] ashaderparameterbool = ShaderParameterBool.values();

        for (ShaderParameterBool shaderparameterbool : ashaderparameterbool) {
            mapExpressions.put(shaderparameterbool.getName(), shaderparameterbool);
        }

        for (BiomeGenBase biomegenbase : BiomeGenBase.BIOME_ID_MAP.values()) {
            String s = biomegenbase.biomeName.trim();
            s = "BIOME_" + s.toUpperCase().replace(' ', '_');
            int j = biomegenbase.biomeID;
            IExpression iexpression = new ConstantFloat((float) j);
            registerExpression(s, iexpression);
        }
    }

    private void addParameterFloat(Map<String, IExpression> map, ShaderParameterFloat spf) {
        String[] astring = spf.getIndexNames1();

        if (astring == null) {
            map.put(spf.getName(), new ShaderParameterIndexed(spf));
        } else {
            for (int i = 0; i < astring.length; ++i) {
                String s = astring[i];
                String[] astring1 = spf.getIndexNames2();

                if (astring1 == null) {
                    map.put(spf.getName() + "." + s, new ShaderParameterIndexed(spf, i));
                } else {
                    for (int j = 0; j < astring1.length; ++j) {
                        String s1 = astring1[j];
                        map.put(spf.getName() + "." + s + "." + s1, new ShaderParameterIndexed(spf, i, j));
                    }
                }
            }
        }
    }

    public boolean registerExpression(String name, IExpression expr) {
        if (mapExpressions.containsKey(name)) {
            SMCLog.warning("Expression already defined: " + name);
            return false;
        } else {
            mapExpressions.put(name, expr);
            return true;
        }
    }

    public IExpression getExpression(String name) {
        return mapExpressions.get(name);
    }

    public boolean hasExpression(String name) {
        return mapExpressions.containsKey(name);
    }
}
