package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S3FPacketCustomPayload implements Packet<INetHandlerPlayClient> {
    private String channel;
    private PacketBuffer data;

    public S3FPacketCustomPayload() {
    }

    public S3FPacketCustomPayload(String channelName, PacketBuffer dataIn) {
        channel = channelName;
        data = dataIn;

        if (dataIn.writerIndex() > 1048576) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        channel = buf.readStringFromBuffer(20);
        int i = buf.readableBytes();

        if (i >= 0 && i <= 1048576) {
            data = new PacketBuffer(buf.readBytes(i));
        } else {
            throw new IOException("Payload may not be larger than 1048576 bytes");
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(channel);
        buf.writeBytes(data);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleCustomPayload(this);
    }

    public String getChannelName() {
        return channel;
    }

    public PacketBuffer getBufferData() {
        return data;
    }
}
