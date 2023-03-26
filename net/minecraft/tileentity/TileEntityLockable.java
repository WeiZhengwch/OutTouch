package net.minecraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public abstract class TileEntityLockable extends TileEntity implements IInteractionObject, ILockableContainer {
    private LockCode code = LockCode.EMPTY_CODE;

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        code = LockCode.fromNBT(compound);
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (code != null) {
            code.toNBT(compound);
        }
    }

    public boolean isLocked() {
        return code != null && !code.isEmpty();
    }

    public LockCode getLockCode() {
        return code;
    }

    public void setLockCode(LockCode code) {
        this.code = code;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName());
    }
}
