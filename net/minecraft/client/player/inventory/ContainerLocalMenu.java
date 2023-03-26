package net.minecraft.client.player.inventory;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

import java.util.Map;

public class ContainerLocalMenu extends InventoryBasic implements ILockableContainer {
    private final String guiID;
    private final Map<Integer, Integer> field_174895_b = Maps.newHashMap();

    public ContainerLocalMenu(String id, IChatComponent title, int slotCount) {
        super(title, slotCount);
        guiID = id;
    }

    public int getField(int id) {
        return field_174895_b.getOrDefault(id, 0);
    }

    public void setField(int id, int value) {
        field_174895_b.put(id, value);
    }

    public int getFieldCount() {
        return field_174895_b.size();
    }

    public boolean isLocked() {
        return false;
    }

    public LockCode getLockCode() {
        return LockCode.EMPTY_CODE;
    }

    public void setLockCode(LockCode code) {
    }

    public String getGuiID() {
        return guiID;
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        throw new UnsupportedOperationException();
    }
}
