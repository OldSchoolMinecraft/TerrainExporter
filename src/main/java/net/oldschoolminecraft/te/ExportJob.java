package net.oldschoolminecraft.te;

public class ExportJob
{
    private Runnable runnable;
    private int x, z;

    public ExportJob(Runnable runnable, int x, int z)
    {
        this.runnable = runnable;
        this.x = x;
        this.z = z;
    }

    public Runnable getRunnable()
    {
        return runnable;
    }

    public int getX()
    {
        return x;
    }

    public int getZ()
    {
        return z;
    }
}
