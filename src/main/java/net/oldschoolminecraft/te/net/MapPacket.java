package net.oldschoolminecraft.te.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class MapPacket
{
    private int packetID;

    public abstract void read(DataInputStream dis) throws IOException;

    public abstract void write(DataOutputStream dos) throws IOException;

    protected String readString(DataInputStream dis, int maxLength) throws IOException
    {
        short stringLen = dis.readShort();
        if (stringLen > maxLength)
        {
            throw new IOException("Received string length longer than maximum allowed (" + stringLen + " > " + maxLength + ")");
        } else if (stringLen < 0) {
            throw new IOException("Received string length is less than zero! Weird string!");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < stringLen; ++i) sb.append(dis.readChar());
            return sb.toString();
        }
    }

    protected void writeString(DataOutputStream dos, String str) throws IOException
    {
        dos.writeShort(str.length());
        for (int i = 0; i < str.length(); ++i) dos.writeChar(str.charAt(i));
    }

    public int getPacketID()
    {
        return packetID;
    }
}
