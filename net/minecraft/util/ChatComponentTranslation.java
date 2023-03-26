package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatComponentTranslation extends ChatComponentStyle {
    public static final Pattern stringVariablePattern = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
    private final String key;
    private final Object[] formatArgs;
    private final Object syncLock = new Object();
    List<IChatComponent> children = Lists.newArrayList();
    private long lastTranslationUpdateTimeInMilliseconds = -1L;

    public ChatComponentTranslation(String translationKey, Object... args) {
        key = translationKey;
        formatArgs = args;

        for (Object object : args) {
            if (object instanceof IChatComponent) {
                ((IChatComponent) object).getChatStyle().setParentStyle(getChatStyle());
            }
        }
    }

    /**
     * ensures that our children are initialized from the most recent string translation mapping.
     */
    synchronized void ensureInitialized() {
        synchronized (syncLock) {
            long i = StatCollector.getLastTranslationUpdateTimeInMilliseconds();

            if (i == lastTranslationUpdateTimeInMilliseconds) {
                return;
            }

            lastTranslationUpdateTimeInMilliseconds = i;
            children.clear();
        }

        try {
            initializeFromFormat(StatCollector.translateToLocal(key));
        } catch (ChatComponentTranslationFormatException chatcomponenttranslationformatexception) {
            children.clear();

            try {
                initializeFromFormat(StatCollector.translateToFallback(key));
            } catch (ChatComponentTranslationFormatException var5) {
                throw chatcomponenttranslationformatexception;
            }
        }
    }

    /**
     * initializes our children from a format string, using the format args to fill in the placeholder variables.
     */
    protected void initializeFromFormat(String format) {
        boolean flag = false;
        Matcher matcher = stringVariablePattern.matcher(format);
        int i = 0;
        int j = 0;

        try {
            int l;

            for (; matcher.find(j); j = l) {
                int k = matcher.start();
                l = matcher.end();

                if (k > j) {
                    ChatComponentText chatcomponenttext = new ChatComponentText(String.format(format.substring(j, k)));
                    chatcomponenttext.getChatStyle().setParentStyle(getChatStyle());
                    children.add(chatcomponenttext);
                }

                String s2 = matcher.group(2);
                String s = format.substring(k, l);

                if ("%".equals(s2) && "%%".equals(s)) {
                    ChatComponentText chatcomponenttext2 = new ChatComponentText("%");
                    chatcomponenttext2.getChatStyle().setParentStyle(getChatStyle());
                    children.add(chatcomponenttext2);
                } else {
                    if (!"s".equals(s2)) {
                        throw new ChatComponentTranslationFormatException(this, "Unsupported format: '" + s + "'");
                    }

                    String s1 = matcher.group(1);
                    int i1 = s1 != null ? Integer.parseInt(s1) - 1 : i++;

                    if (i1 < formatArgs.length) {
                        children.add(getFormatArgumentAsComponent(i1));
                    }
                }
            }

            if (j < format.length()) {
                ChatComponentText chatcomponenttext1 = new ChatComponentText(String.format(format.substring(j)));
                chatcomponenttext1.getChatStyle().setParentStyle(getChatStyle());
                children.add(chatcomponenttext1);
            }
        } catch (IllegalFormatException illegalformatexception) {
            throw new ChatComponentTranslationFormatException(this, illegalformatexception);
        }
    }

    private IChatComponent getFormatArgumentAsComponent(int index) {
        if (index >= formatArgs.length) {
            throw new ChatComponentTranslationFormatException(this, index);
        } else {
            Object object = formatArgs[index];
            IChatComponent ichatcomponent;

            if (object instanceof IChatComponent) {
                ichatcomponent = (IChatComponent) object;
            } else {
                ichatcomponent = new ChatComponentText(object == null ? "null" : object.toString());
                ichatcomponent.getChatStyle().setParentStyle(getChatStyle());
            }

            return ichatcomponent;
        }
    }

    public IChatComponent setChatStyle(ChatStyle style) {
        super.setChatStyle(style);

        for (Object object : formatArgs) {
            if (object instanceof IChatComponent) {
                ((IChatComponent) object).getChatStyle().setParentStyle(getChatStyle());
            }
        }

        if (lastTranslationUpdateTimeInMilliseconds > -1L) {
            for (IChatComponent ichatcomponent : children) {
                ichatcomponent.getChatStyle().setParentStyle(style);
            }
        }

        return this;
    }

    public Iterator<IChatComponent> iterator() {
        ensureInitialized();
        return Iterators.concat(createDeepCopyIterator(children), createDeepCopyIterator(siblings));
    }

    /**
     * Gets the text of this component, without any special formatting codes added, for chat.  TODO: why is this two
     * different methods?
     */
    public String getUnformattedTextForChat() {
        ensureInitialized();
        StringBuilder stringbuilder = new StringBuilder();

        for (IChatComponent ichatcomponent : children) {
            stringbuilder.append(ichatcomponent.getUnformattedTextForChat());
        }

        return stringbuilder.toString();
    }

    /**
     * Creates a copy of this component.  Almost a deep copy, except the style is shallow-copied.
     */
    public ChatComponentTranslation createCopy() {
        Object[] aobject = new Object[formatArgs.length];

        for (int i = 0; i < formatArgs.length; ++i) {
            if (formatArgs[i] instanceof IChatComponent) {
                aobject[i] = ((IChatComponent) formatArgs[i]).createCopy();
            } else {
                aobject[i] = formatArgs[i];
            }
        }

        ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(key, aobject);
        chatcomponenttranslation.setChatStyle(getChatStyle().createShallowCopy());

        for (IChatComponent ichatcomponent : getSiblings()) {
            chatcomponenttranslation.appendSibling(ichatcomponent.createCopy());
        }

        return chatcomponenttranslation;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChatComponentTranslation chatcomponenttranslation)) {
            return false;
        } else {
            return Arrays.equals(formatArgs, chatcomponenttranslation.formatArgs) && key.equals(chatcomponenttranslation.key) && super.equals(p_equals_1_);
        }
    }

    public int hashCode() {
        int i = super.hashCode();
        i = 31 * i + key.hashCode();
        i = 31 * i + Arrays.hashCode(formatArgs);
        return i;
    }

    public String toString() {
        return "TranslatableComponent{key='" + key + '\'' + ", args=" + Arrays.toString(formatArgs) + ", siblings=" + siblings + ", style=" + getChatStyle() + '}';
    }

    public String getKey() {
        return key;
    }

    public Object[] getFormatArgs() {
        return formatArgs;
    }
}
