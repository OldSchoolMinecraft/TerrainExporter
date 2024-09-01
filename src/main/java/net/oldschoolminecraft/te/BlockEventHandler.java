package net.oldschoolminecraft.te;

import net.oldschoolminecraft.te.job.ExportJob;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEventHandler implements Listener
{
    private ExportManager exportManager = TerrainExporter.getInstance().getExportManager();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Location blockLoc = event.getBlock().getLocation();
        int highestY = event.getBlock().getLocation().getWorld().getHighestBlockYAt(blockLoc) - 1;
        if (highestY > blockLoc.getBlockY()) return; // there are blocks above it, the change won't be visible
        int chunkX = Utils.calculateChunkCoordinate(blockLoc.getBlockX());
        int chunkZ = Utils.calculateChunkCoordinate(blockLoc.getBlockZ());
        exportManager.queueJob(new ExportJob(chunkX, chunkZ));

        System.out.println("=== " + event.getPlayer().getName() + " triggered an export job @ " + chunkX + "," + chunkZ + "! ===");
    }



    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Location blockLoc = event.getBlock().getLocation();
        int highestY = event.getBlock().getLocation().getWorld().getHighestBlockYAt(blockLoc) - 1;
        if (highestY > blockLoc.getBlockY()) return; // there are blocks above it, the change won't be visible
        int chunkX = Utils.calculateChunkCoordinate(blockLoc.getBlockX());
        int chunkZ = Utils.calculateChunkCoordinate(blockLoc.getBlockZ());
        exportManager.queueJob(new ExportJob(chunkX, chunkZ));

        System.out.println("=== " + event.getPlayer().getName() + " triggered an export job @ " + chunkX + "," + chunkZ + "! ===");
    }
}
