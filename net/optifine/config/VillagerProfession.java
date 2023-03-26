package net.optifine.config;

import net.minecraft.src.Config;

public class VillagerProfession {
    private final int profession;
    private int[] careers;

    public VillagerProfession(int profession) {
        this(profession, null);
    }

    public VillagerProfession(int profession, int career) {
        this(profession, new int[]{career});
    }

    public VillagerProfession(int profession, int[] careers) {
        this.profession = profession;
        this.careers = careers;
    }

    public boolean matches(int prof, int car) {
        return profession == prof && (careers == null || Config.equalsOne(car, careers));
    }

    private boolean hasCareer(int car) {
        return careers != null && Config.equalsOne(car, careers);
    }

    public boolean addCareer(int car) {
        if (careers == null) {
            careers = new int[]{car};
            return true;
        } else if (hasCareer(car)) {
            return false;
        } else {
            careers = Config.addIntToArray(careers, car);
            return true;
        }
    }

    public int getProfession() {
        return profession;
    }

    public int[] getCareers() {
        return careers;
    }

    public String toString() {
        return careers == null ? String.valueOf(profession) : profession + ":" + Config.arrayToString(careers);
    }
}
