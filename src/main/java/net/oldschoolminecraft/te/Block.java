package net.oldschoolminecraft.te;

public class Block
{
    private byte id, data, height;

    public Block(byte id, byte data, byte height)
    {
        this.id = id;
        this.data = data;
        this.height = height;
    }

    public byte getID()
    {
        return id;
    }

    public byte getData()
    {
        return data;
    }

    public byte getHeight()
    {
        return height;
    }
}
