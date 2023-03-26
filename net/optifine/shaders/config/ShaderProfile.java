package net.optifine.shaders.config;

import java.util.*;

public class ShaderProfile {
    private final Map<String, String> mapOptionValues = new LinkedHashMap();
    private final Set<String> disabledPrograms = new LinkedHashSet();
    private final String name;

    public ShaderProfile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addOptionValue(String option, String value) {
        mapOptionValues.put(option, value);
    }

    public void addOptionValues(ShaderProfile prof) {
        if (prof != null) {
            mapOptionValues.putAll(prof.mapOptionValues);
        }
    }

    public void applyOptionValues(ShaderOption[] options) {
        for (ShaderOption shaderoption : options) {
            String s = shaderoption.getName();
            String s1 = mapOptionValues.get(s);

            if (s1 != null) {
                shaderoption.setValue(s1);
            }
        }
    }

    public String[] getOptions() {
        Set<String> set = mapOptionValues.keySet();
        String[] astring = set.toArray(new String[set.size()]);
        return astring;
    }

    public String getValue(String key) {
        return mapOptionValues.get(key);
    }

    public void addDisabledProgram(String program) {
        disabledPrograms.add(program);
    }

    public void removeDisabledProgram(String program) {
        disabledPrograms.remove(program);
    }

    public Collection<String> getDisabledPrograms() {
        return new LinkedHashSet(disabledPrograms);
    }

    public void addDisabledPrograms(Collection<String> programs) {
        disabledPrograms.addAll(programs);
    }

    public boolean isProgramDisabled(String program) {
        return disabledPrograms.contains(program);
    }
}
