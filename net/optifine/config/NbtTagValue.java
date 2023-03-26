package net.optifine.config;

import net.minecraft.nbt.*;
import net.minecraft.src.Config;
import net.optifine.util.StrUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

public class NbtTagValue {
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_PATTERN = 1;
    private static final int TYPE_IPATTERN = 2;
    private static final int TYPE_REGEX = 3;
    private static final int TYPE_IREGEX = 4;
    private static final String PREFIX_PATTERN = "pattern:";
    private static final String PREFIX_IPATTERN = "ipattern:";
    private static final String PREFIX_REGEX = "regex:";
    private static final String PREFIX_IREGEX = "iregex:";
    private static final int FORMAT_DEFAULT = 0;
    private static final int FORMAT_HEX_COLOR = 1;
    private static final String PREFIX_HEX_COLOR = "#";
    private static final Pattern PATTERN_HEX_COLOR = Pattern.compile("^#[0-9a-f]{6}+$");
    private final String[] parents;
    private final String name;
    private boolean negative;
    private final int type;
    private final String value;
    private int valueFormat;

    public NbtTagValue(String tag, String value) {
        String[] astring = Config.tokenize(tag, ".");
        parents = Arrays.copyOfRange(astring, 0, astring.length - 1);
        name = astring[astring.length - 1];

        if (value.startsWith("!")) {
            negative = true;
            value = value.substring(1);
        }

        if (value.startsWith("pattern:")) {
            type = 1;
            value = value.substring("pattern:".length());
        } else if (value.startsWith("ipattern:")) {
            type = 2;
            value = value.substring("ipattern:".length()).toLowerCase();
        } else if (value.startsWith("regex:")) {
            type = 3;
            value = value.substring("regex:".length());
        } else if (value.startsWith("iregex:")) {
            type = 4;
            value = value.substring("iregex:".length()).toLowerCase();
        } else {
            type = 0;
        }

        value = StringEscapeUtils.unescapeJava(value);

        if (type == 0 && PATTERN_HEX_COLOR.matcher(value).matches()) {
            valueFormat = 1;
        }

        this.value = value;
    }

    private static NBTBase getChildTag(NBTBase tagBase, String tag) {
        if (tagBase instanceof NBTTagCompound nbttagcompound) {
            return nbttagcompound.getTag(tag);
        } else if (tagBase instanceof NBTTagList nbttaglist) {

            if (tag.equals("count")) {
                return new NBTTagInt(nbttaglist.tagCount());
            } else {
                int i = Config.parseInt(tag, -1);
                return i >= 0 && i < nbttaglist.tagCount() ? nbttaglist.get(i) : null;
            }
        } else {
            return null;
        }
    }

    private static String getNbtString(NBTBase nbtBase, int format) {
        if (nbtBase == null) {
            return null;
        } else if (nbtBase instanceof NBTTagString nbttagstring) {
            return nbttagstring.getString();
        } else if (nbtBase instanceof NBTTagInt nbttagint) {
            return format == 1 ? "#" + StrUtils.fillLeft(Integer.toHexString(nbttagint.getInt()), 6, '0') : Integer.toString(nbttagint.getInt());
        } else if (nbtBase instanceof NBTTagByte nbttagbyte) {
            return Byte.toString(nbttagbyte.getByte());
        } else if (nbtBase instanceof NBTTagShort nbttagshort) {
            return Short.toString(nbttagshort.getShort());
        } else if (nbtBase instanceof NBTTagLong nbttaglong) {
            return Long.toString(nbttaglong.getLong());
        } else if (nbtBase instanceof NBTTagFloat nbttagfloat) {
            return Float.toString(nbttagfloat.getFloat());
        } else if (nbtBase instanceof NBTTagDouble nbttagdouble) {
            return Double.toString(nbttagdouble.getDouble());
        } else {
            return nbtBase.toString();
        }
    }

    public boolean matches(NBTTagCompound nbt) {
        return negative != matchesCompound(nbt);
    }

    public boolean matchesCompound(NBTTagCompound nbt) {
        if (nbt == null) {
            return false;
        } else {
            NBTBase nbtbase = nbt;

            for (String s : parents) {
                nbtbase = getChildTag(nbtbase, s);

                if (nbtbase == null) {
                    return false;
                }
            }

            if (name.equals("*")) {
                return matchesAnyChild(nbtbase);
            } else {
                nbtbase = getChildTag(nbtbase, name);

                if (nbtbase == null) {
                    return false;
                } else return matchesBase(nbtbase);
            }
        }
    }

    private boolean matchesAnyChild(NBTBase tagBase) {
        if (tagBase instanceof NBTTagCompound nbttagcompound) {

            for (String s : nbttagcompound.getKeySet()) {
                NBTBase nbtbase = nbttagcompound.getTag(s);

                if (matchesBase(nbtbase)) {
                    return true;
                }
            }
        }

        if (tagBase instanceof NBTTagList nbttaglist) {
            int i = nbttaglist.tagCount();

            for (int j = 0; j < i; ++j) {
                NBTBase nbtbase1 = nbttaglist.get(j);

                if (matchesBase(nbtbase1)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean matchesBase(NBTBase nbtBase) {
        if (nbtBase == null) {
            return false;
        } else {
            String s = getNbtString(nbtBase, valueFormat);
            return matchesValue(s);
        }
    }

    public boolean matchesValue(String nbtValue) {
        if (nbtValue == null) {
            return false;
        } else {
            return switch (type) {
                case 0 -> nbtValue.equals(value);
                case 1 -> matchesPattern(nbtValue, value);
                case 2 -> matchesPattern(nbtValue.toLowerCase(), value);
                case 3 -> matchesRegex(nbtValue, value);
                case 4 -> matchesRegex(nbtValue.toLowerCase(), value);
                default -> throw new IllegalArgumentException("Unknown NbtTagValue type: " + type);
            };
        }
    }

    private boolean matchesPattern(String str, String pattern) {
        return StrUtils.equalsMask(str, pattern, '*', '?');
    }

    private boolean matchesRegex(String str, String regex) {
        return str.matches(regex);
    }

    public String toString() {
        StringBuffer stringbuffer = new StringBuffer();

        for (int i = 0; i < parents.length; ++i) {
            String s = parents[i];

            if (i > 0) {
                stringbuffer.append(".");
            }

            stringbuffer.append(s);
        }

        if (stringbuffer.length() > 0) {
            stringbuffer.append(".");
        }

        stringbuffer.append(name);
        stringbuffer.append(" = ");
        stringbuffer.append(value);
        return stringbuffer.toString();
    }
}
