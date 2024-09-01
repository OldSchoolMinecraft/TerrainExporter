package net.oldschoolminecraft.te;

public class Utils
{
    public static int calculateChunkCoordinate(double coordinate)
    {
        return (int) Math.floor(coordinate / 16);
    }
}
