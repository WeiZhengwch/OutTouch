package net.minecraft.realms;

import net.minecraft.util.IChatComponent;

import java.util.List;

public class DisconnectedRealmsScreen extends RealmsScreen {
    private final String title;
    private final IChatComponent reason;
    private final RealmsScreen parent;
    private List<String> lines;
    private int textHeight;

    public DisconnectedRealmsScreen(RealmsScreen parentIn, String unlocalizedTitle, IChatComponent reasonIn) {
        parent = parentIn;
        title = getLocalizedString(unlocalizedTitle);
        reason = reasonIn;
    }

    public void init() {
        Realms.setConnectedToRealms(false);
        buttonsClear();
        lines = fontSplit(reason.getFormattedText(), width() - 50);
        textHeight = lines.size() * fontLineHeight();
        buttonsAdd(newButton(0, width() / 2 - 100, height() / 2 + textHeight / 2 + fontLineHeight(), getLocalizedString("gui.back")));
    }

    public void keyPressed(char p_keyPressed_1_, int p_keyPressed_2_) {
        if (p_keyPressed_2_ == 1) {
            Realms.setScreen(parent);
        }
    }

    public void buttonClicked(RealmsButton p_buttonClicked_1_) {
        if (p_buttonClicked_1_.id() == 0) {
            Realms.setScreen(parent);
        }
    }

    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground();
        drawCenteredString(title, width() / 2, height() / 2 - textHeight / 2 - fontLineHeight() * 2, 11184810);
        int i = height() / 2 - textHeight / 2;

        if (lines != null) {
            for (String s : lines) {
                drawCenteredString(s, width() / 2, i, 16777215);
                i += fontLineHeight();
            }
        }

        super.render(p_render_1_, p_render_2_, p_render_3_);
    }
}
