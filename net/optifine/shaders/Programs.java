package net.optifine.shaders;

import java.util.ArrayList;
import java.util.List;

public class Programs {
    private final List<Program> programs = new ArrayList();
    private final Program programNone = make("", ProgramStage.NONE, true);

    public Program make(String name, ProgramStage programStage, Program backupProgram) {
        int i = programs.size();
        Program program = new Program(i, name, programStage, backupProgram);
        programs.add(program);
        return program;
    }

    private Program make(String name, ProgramStage programStage, boolean ownBackup) {
        int i = programs.size();
        Program program = new Program(i, name, programStage, ownBackup);
        programs.add(program);
        return program;
    }

    public Program makeGbuffers(String name, Program backupProgram) {
        return make(name, ProgramStage.GBUFFERS, backupProgram);
    }

    public Program makeComposite(String name) {
        return make(name, ProgramStage.COMPOSITE, programNone);
    }

    public Program makeDeferred(String name) {
        return make(name, ProgramStage.DEFERRED, programNone);
    }

    public Program makeShadow(String name, Program backupProgram) {
        return make(name, ProgramStage.SHADOW, backupProgram);
    }

    public Program makeVirtual(String name) {
        return make(name, ProgramStage.NONE, true);
    }

    public Program[] makeComposites(String prefix, int count) {
        Program[] aprogram = new Program[count];

        for (int i = 0; i < count; ++i) {
            String s = i == 0 ? prefix : prefix + i;
            aprogram[i] = makeComposite(s);
        }

        return aprogram;
    }

    public Program[] makeDeferreds(String prefix, int count) {
        Program[] aprogram = new Program[count];

        for (int i = 0; i < count; ++i) {
            String s = i == 0 ? prefix : prefix + i;
            aprogram[i] = makeDeferred(s);
        }

        return aprogram;
    }

    public Program getProgramNone() {
        return programNone;
    }

    public int getCount() {
        return programs.size();
    }

    public Program getProgram(String name) {
        if (name == null) {
            return null;
        } else {
            for (Program program : programs) {
                String s = program.getName();

                if (s.equals(name)) {
                    return program;
                }
            }

            return null;
        }
    }

    public String[] getProgramNames() {
        String[] astring = new String[programs.size()];

        for (int i = 0; i < astring.length; ++i) {
            astring[i] = programs.get(i).getName();
        }

        return astring;
    }

    public Program[] getPrograms() {
        Program[] aprogram = programs.toArray(new Program[programs.size()]);
        return aprogram;
    }

    public Program[] getPrograms(Program programFrom, Program programTo) {
        int i = programFrom.getIndex();
        int j = programTo.getIndex();

        if (i > j) {
            int k = i;
            i = j;
            j = k;
        }

        Program[] aprogram = new Program[j - i + 1];

        for (int l = 0; l < aprogram.length; ++l) {
            aprogram[l] = programs.get(i + l);
        }

        return aprogram;
    }

    public String toString() {
        return programs.toString();
    }
}
