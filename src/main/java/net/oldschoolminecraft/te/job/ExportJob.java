package net.oldschoolminecraft.te.job;

import net.oldschoolminecraft.te.Utils;

public class ExportJob implements IJob
{
    private int x, z;
    private final Runnable runnable;

    public ExportJob(int x, int z)
    {
        this.x = x;
        this.z = z;
        this.runnable = () ->
        {
            Utils.exportChunk(x, z);
            System.out.println("(JOB " + getTruncatedHash() + ") Single chunk export @ " + x + "," + z + " completed!");
        };
    }

    public int getX()
    {
        return x;
    }

    public int getZ()
    {
        return z;
    }

    public Runnable getRunnable()
    {
        return runnable;
    }

    public String getHash()
    {
        return Utils.buildHash(x, z);
    }

    public String getTruncatedHash()
    {
        String first = getHash().substring(0, 6);
        String last = getHash().substring(getHash().length() - 6);

        return first + last;
    }
}
