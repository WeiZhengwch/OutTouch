package net.minecraft.util;

public class ChatComponentSelector extends ChatComponentStyle {
    /**
     * The selector used to find the matching entities of this text component
     */
    private final String selector;

    public ChatComponentSelector(String selectorIn) {
        selector = selectorIn;
    }

    /**
     * Gets the selector of this component, in plain text.
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Gets the text of this component, without any special formatting codes added, for chat.  TODO: why is this two
     * different methods?
     */
    public String getUnformattedTextForChat() {
        return selector;
    }

    /**
     * Creates a copy of this component.  Almost a deep copy, except the style is shallow-copied.
     */
    public ChatComponentSelector createCopy() {
        ChatComponentSelector chatcomponentselector = new ChatComponentSelector(selector);
        chatcomponentselector.setChatStyle(getChatStyle().createShallowCopy());

        for (IChatComponent ichatcomponent : getSiblings()) {
            chatcomponentselector.appendSibling(ichatcomponent.createCopy());
        }

        return chatcomponentselector;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChatComponentSelector chatcomponentselector)) {
            return false;
        } else {
            return selector.equals(chatcomponentselector.selector) && super.equals(p_equals_1_);
        }
    }

    public String toString() {
        return "SelectorComponent{pattern='" + selector + '\'' + ", siblings=" + siblings + ", style=" + getChatStyle() + '}';
    }
}
