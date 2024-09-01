package net.oldschoolminecraft.te.job;

import net.oldschoolminecraft.te.Utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class BulkExportJob implements IBulkJob
{
    private int x, z;
    private int radius;
    private Runnable runnable;

    public BulkExportJob(int x, int z, int radius)
    {
        this.x = x;
        this.z = z;
        this.radius = radius;
        this.runnable = () ->
        {
            System.out.println("(JOB " + getTruncatedHash() + ") Starting bulk terrain export job: " + x + "," + z + " - radius: " + radius);

            int chunkCount = 0;
            for (int dx = -radius; dx <= radius; dx++)
            {
                for (int dz = -radius; dz <= radius; dz++)
                {
                    if (chunkCount == (getChunkCount() / 2))
                        System.out.println("(JOB " + getTruncatedHash() + ") Bulk terrain export is 50% complete.");

                    int chunkX = x + dx;
                    int chunkZ = z + dz;
                    Utils.exportChunk(chunkX, chunkZ);
                    chunkCount++;
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
                }
            }
            System.out.println("(JOB " + getTruncatedHash() + ") Bulk export job completed. Processed " + chunkCount + " chunks");
        };
    }

    @Override
    public int getRadius()
    {
        return radius;
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
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
        return Utils.buildHash(x, z, radius);
    }

    public String getTruncatedHash()
    {
        String first = getHash().substring(0, 6);
        String last = getHash().substring(getHash().length() - 6);

        return first + last;
    }

    public int getChunkCount()
    {
        int sideLength = (2 * radius) + 1;
        return sideLength * sideLength;
    }
}
