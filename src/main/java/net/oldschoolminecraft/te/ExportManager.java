package net.oldschoolminecraft.te;

import net.minecraft.server.Packet;
import net.oldschoolminecraft.te.job.IJob;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ExportManager
{
    private LinkedList<IJob> jobs;
    private boolean immediateMode = false;

    public ExportManager()
    {
        jobs = new LinkedList<>();
    }

    public void runImmediateJob(IJob job)
    {
        immediateMode = true;
        new Thread(() ->
        {
            job.getRunnable().run();
            immediateMode = false;
        }).start();

    }

    public void runAllJobs(int waitMS)
    {
        if (immediateMode) return;
        if (jobs.isEmpty()) return;
        while (!jobs.isEmpty())
        {
            while (immediateMode)
            {
                // lock the loop until the immediate-mode task is finished.
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
            }

            IJob job = popQueue();
            System.out.println("Running job for chunk " + job.getX() + "," + job.getZ());
            job.getRunnable().run();

            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
        }
    }

    public boolean queueJob(IJob job)
    {
        if (hasJob(job)) return false;
        jobs.add(job);
        return true;
    }

    private boolean hasJob(IJob job)
    {
        return hasJob(job.getHash());
    }

    private boolean hasJob(String jobHash)
    {
        for (IJob job : jobs)
            if (job.getHash().equals(jobHash))
                return true;
        return false;
    }

    public boolean isImmediateMode()
    {
        return immediateMode;
    }

    public IJob popQueue()
    {
        return jobs.pop();
    }

    public int getQueueSize()
    {
        return jobs.size();
    }
}
