package net.oldschoolminecraft.te.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MapNegotiate extends MapPacket
{
    private String uuid;

    @Override
    public void read(DataInputStream dis) throws IOException
    {
        this.uuid = readString(dis, 36);
    }

    @Override
    public void write(DataOutputStream dos) throws IOException
    {

    }
}
