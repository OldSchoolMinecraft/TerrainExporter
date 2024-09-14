package net.oldschoolminecraft.te.net;

import net.oldschoolminecraft.te.Block;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MapChunkData extends MapPacket
{
    private Block[] chunkData;

    @Override
    public void read(DataInputStream dis) throws IOException
    {
        // read array length
        // initialize array
        // read 3 bytes * 16 for each block
    }

    @Override
    public void write(DataOutputStream dos) throws IOException
    {
    }
}
