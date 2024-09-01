package net.oldschoolminecraft.te.job;

public interface IJob
{
    int getX();
    int getZ();
    Runnable getRunnable();
    String getHash();
    String getTruncatedHash();
}
