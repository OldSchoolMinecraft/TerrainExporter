package net.oldschoolminecraft.te;

import net.oldschoolminecraft.te.job.BulkExportJob;
import net.oldschoolminecraft.te.job.ExportJob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TerrainExporter extends JavaPlugin
{
    private static TerrainExporter instance;

    public static TerrainExporter getInstance()
    {
        return instance;
    }

    private ExportManager exportManager;
    private int taskID = -1;

    public void onEnable()
    {
        instance = this;
        exportManager = new ExportManager();

        // Command to export current chunk terrain data
        getCommand("savechunk").setExecutor((sender, command, label, args) ->
        {
            if (!(sender.hasPermission("te.save") || sender.isOp()))
            {
                sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
                return true;
            }
            boolean privileged = sender.isOp();
            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                int chunkX = Utils.calculateChunkCoordinate(player.getLocation().getX());
                int chunkZ = Utils.calculateChunkCoordinate(player.getLocation().getZ());

                if (args.length > 0)
                {
                    try
                    {
                        int radius = Integer.parseInt(args[0]);
                        if (radius > 16 && !(sender.hasPermission("te.savebig") || sender.isOp()))
                        {
                            sender.sendMessage(ChatColor.RED + "A radius higher than 16 is not permitted.");
                            return true;
                        }

                        BulkExportJob bulkExportJob = new BulkExportJob(chunkX, chunkZ, radius);
                        sender.sendMessage(ChatColor.GREEN + String.format("Running chunk export job for %s chunks", bulkExportJob.getChunkCount()));
                        ChatColor privColor = privileged ? ChatColor.RED : ChatColor.GREEN;
                        sender.sendMessage(ChatColor.GRAY + "Status: " + privColor + (privileged ? "Immediate Mode (Privileged)" : "Queued"));

                        if (privileged) exportManager.runImmediateJob(bulkExportJob);
                        else exportManager.queueJob(bulkExportJob);
                        return true;
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(ChatColor.RED + "You must provide a number");
                        return true;
                    }
                }

                sender.sendMessage(ChatColor.GREEN + String.format("Running single chunk export job @ %s,%s", chunkX, chunkZ));
                ChatColor privColor = privileged ? ChatColor.RED : ChatColor.GREEN;
                sender.sendMessage(ChatColor.GRAY + "Status: " + privColor + (privileged ? "Immediate Mode (Privileged)" : "Queued"));

                if (privileged) exportManager.runImmediateJob(new ExportJob(chunkX, chunkZ));
                else exportManager.queueJob(new ExportJob(chunkX, chunkZ));
                return true;
            } else {
                sender.sendMessage("This command can only be executed by a player.");
                return false;
            }
        });

        taskID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () ->
        {
            if (exportManager.getQueueSize() < 1) return;
            if (exportManager.isImmediateMode())
            {
                System.out.println("Terrain export delayed because the export manager is in immediate mode.");
                System.out.println("The scheduled queue processing will resume when the immediate mode job has completed.");
                while (exportManager.isImmediateMode()) LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
            }

            System.out.println("=== Processing terrain export queue! ===");
            exportManager.runAllJobs(500); // run all current jobs, waiting 500ms between each job
            System.out.println("=== Finished processing terrain export queue! ===");
        }, 6000, 6000); // 6k tick delay (5 minutes), 6k tick timer (5 minutes)

        // register events for chunk update triggers
        getServer().getPluginManager().registerEvents(new BlockEventHandler(), this);

        System.out.println("TerrainExporter enabled");
    }

    public ExportManager getExportManager()
    {
        return exportManager;
    }

    public void onDisable()
    {
        System.out.println("TerrainExporter disabled");
    }
}
