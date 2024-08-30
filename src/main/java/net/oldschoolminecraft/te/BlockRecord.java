package net.oldschoolminecraft.te;

public class BlockRecord
{
    public int x, y, z, blockID;

    public BlockRecord(int x, int y, int z, int blockID)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockID = blockID;
    }

    public String toString()
    {
        return blockID + " @ " + x + ", " + y + ", " + z;
    }
}
