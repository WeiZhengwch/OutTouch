package net.minecraft.client.renderer;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Collection;

@SuppressWarnings("ALL")
public abstract class InventoryEffectRenderer extends GuiContainer {
    /**
     * True if there is some potion effect to display
     */
    private boolean hasActivePotionEffects;

    public InventoryEffectRenderer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        super.initGui();
        updateActivePotionEffects();
    }

    protected void updateActivePotionEffects() {
        if (!mc.thePlayer.getActivePotionEffects().isEmpty()) {
            guiLeft = 160 + (width - xSize - 200) / 2;
            hasActivePotionEffects = true;
        } else {
            guiLeft = (width - xSize) / 2;
            hasActivePotionEffects = false;
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (hasActivePotionEffects) {
            drawActivePotionEffects();
        }
    }

    /**
     * Display the potion effects list
     */
    public void drawActivePotionEffects() {
        int i = guiLeft - 124;
        int j = guiTop;
        int k = 166;
        Collection<PotionEffect> collection = mc.thePlayer.getActivePotionEffects();

        if (!collection.isEmpty()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            int l = 33;

            if (collection.size() > 5) {
                l = 132 / (collection.size() - 1);
            }

            for (PotionEffect potioneffect : mc.thePlayer.getActivePotionEffects()) {
                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(inventoryBackground);
                drawTexturedModalRect(i, j, 0, 166, 140, 32);

                if (potion.hasStatusIcon()) {
                    int i1 = potion.getStatusIconIndex();
                    drawTexturedModalRect(i + 6, j + 7, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                }

                String s1 = I18n.format(potion.getName());

                if (potioneffect.getAmplifier() == 1) {
                    s1 = s1 + " " + I18n.format("enchantment.level.2");
                } else if (potioneffect.getAmplifier() == 2) {
                    s1 = s1 + " " + I18n.format("enchantment.level.3");
                } else if (potioneffect.getAmplifier() == 3) {
                    s1 = s1 + " " + I18n.format("enchantment.level.4");
                }

                fontRendererObj.drawStringWithShadow(s1, (float) (i + 10 + 18), (float) (j + 6), 16777215);
                String s = Potion.getDurationString(potioneffect);
                fontRendererObj.drawStringWithShadow(s, (float) (i + 10 + 18), (float) (j + 6 + 10), 8355711);
                j += l;
            }
        }
    }
}
