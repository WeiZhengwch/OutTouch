package net.minecraft.network.play.server;

import net.minecraft.entity.item.EntityPainting;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.io.IOException;

public class S10PacketSpawnPainting implements Packet<INetHandlerPlayClient> {
    private int entityID;
    private BlockPos position;
    private EnumFacing facing;
    private String title;

    public S10PacketSpawnPainting() {
    }

    public S10PacketSpawnPainting(EntityPainting painting) {
        entityID = painting.getEntityId();
        position = painting.getHangingPosition();
        facing = painting.facingDirection;
        title = painting.art.title;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        entityID = buf.readVarIntFromBuffer();
        title = buf.readStringFromBuffer(EntityPainting.EnumArt.field_180001_A);
        position = buf.readBlockPos();
        facing = EnumFacing.getHorizontal(buf.readUnsignedByte());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(entityID);
        buf.writeString(title);
        buf.writeBlockPos(position);
        buf.writeByte(facing.getHorizontalIndex());
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleSpawnPainting(this);
    }

    public int getEntityID() {
        return entityID;
    }

    public BlockPos getPosition() {
        return position;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public String getTitle() {
        return title;
    }
}
