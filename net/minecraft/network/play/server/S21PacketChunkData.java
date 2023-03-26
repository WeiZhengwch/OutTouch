package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.io.IOException;
import java.util.List;

public class S21PacketChunkData implements Packet<INetHandlerPlayClient> {
    private int chunkX;
    private int chunkZ;
    private S21PacketChunkData.Extracted extractedData;
    private boolean field_149279_g;

    public S21PacketChunkData() {
    }

    public S21PacketChunkData(Chunk chunkIn, boolean p_i45196_2_, int p_i45196_3_) {
        chunkX = chunkIn.xPosition;
        chunkZ = chunkIn.zPosition;
        field_149279_g = p_i45196_2_;
        extractedData = getExtractedData(chunkIn, p_i45196_2_, !chunkIn.getWorld().provider.getHasNoSky(), p_i45196_3_);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        field_149279_g = buf.readBoolean();
        extractedData = new S21PacketChunkData.Extracted();
        extractedData.dataSize = buf.readShort();
        extractedData.data = buf.readByteArray();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(field_149279_g);
        buf.writeShort((short) (extractedData.dataSize & 65535));
        buf.writeByteArray(extractedData.data);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleChunkData(this);
    }

    public byte[] getExtractedDataBytes() {
        return extractedData.data;
    }

    protected static int func_180737_a(int p_180737_0_, boolean p_180737_1_, boolean p_180737_2_) {
        int i = p_180737_0_ * 2 * 16 * 16 * 16;
        int j = p_180737_0_ * 16 * 16 * 16 / 2;
        int k = p_180737_1_ ? p_180737_0_ * 16 * 16 * 16 / 2 : 0;
        int l = p_180737_2_ ? 256 : 0;
        return i + j + k + l;
    }

    public static S21PacketChunkData.Extracted getExtractedData(Chunk p_179756_0_, boolean p_179756_1_, boolean p_179756_2_, int p_179756_3_) {
        ExtendedBlockStorage[] aextendedblockstorage = p_179756_0_.getBlockStorageArray();
        S21PacketChunkData.Extracted s21packetchunkdata$extracted = new S21PacketChunkData.Extracted();
        List<ExtendedBlockStorage> list = Lists.newArrayList();

        for (int i = 0; i < aextendedblockstorage.length; ++i) {
            ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[i];

            if (extendedblockstorage != null && (!p_179756_1_ || !extendedblockstorage.isEmpty()) && (p_179756_3_ & 1 << i) != 0) {
                s21packetchunkdata$extracted.dataSize |= 1 << i;
                list.add(extendedblockstorage);
            }
        }

        s21packetchunkdata$extracted.data = new byte[func_180737_a(Integer.bitCount(s21packetchunkdata$extracted.dataSize), p_179756_2_, p_179756_1_)];
        int j = 0;

        for (ExtendedBlockStorage extendedblockstorage1 : list) {
            char[] achar = extendedblockstorage1.getData();

            for (char c0 : achar) {
                s21packetchunkdata$extracted.data[j++] = (byte) (c0 & 255);
                s21packetchunkdata$extracted.data[j++] = (byte) (c0 >> 8 & 255);
            }
        }

        for (ExtendedBlockStorage extendedblockstorage2 : list) {
            j = func_179757_a(extendedblockstorage2.getBlocklightArray().getData(), s21packetchunkdata$extracted.data, j);
        }

        if (p_179756_2_) {
            for (ExtendedBlockStorage extendedblockstorage3 : list) {
                j = func_179757_a(extendedblockstorage3.getSkylightArray().getData(), s21packetchunkdata$extracted.data, j);
            }
        }

        if (p_179756_1_) {
            func_179757_a(p_179756_0_.getBiomeArray(), s21packetchunkdata$extracted.data, j);
        }

        return s21packetchunkdata$extracted;
    }

    private static int func_179757_a(byte[] p_179757_0_, byte[] p_179757_1_, int p_179757_2_) {
        System.arraycopy(p_179757_0_, 0, p_179757_1_, p_179757_2_, p_179757_0_.length);
        return p_179757_2_ + p_179757_0_.length;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getExtractedSize() {
        return extractedData.dataSize;
    }

    public boolean func_149274_i() {
        return field_149279_g;
    }

    public static class Extracted {
        public byte[] data;
        public int dataSize;
    }
}
