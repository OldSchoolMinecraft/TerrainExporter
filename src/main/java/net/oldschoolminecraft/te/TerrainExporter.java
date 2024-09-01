package net.oldschoolminecraft.te;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getScheduler;

public class TerrainExporter extends JavaPlugin
{
    private static final int CHUNK_SIZE = 16;
    private static TerrainExporter instance;

    public static TerrainExporter getInstance()
    {
        return instance;
    }

    public void onEnable()
    {
        instance = this;

        // Command to export current chunk terrain data
        getCommand("savechunk").setExecutor((sender, command, label, args) ->
        {
            if (!(sender.hasPermission("te.save") || sender.isOp()))
            {
                sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
                return true;
            }
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

                        sender.sendMessage(ChatColor.GREEN + "Your chunk export job has been added to the queue!");

                        ExportManager.getInstance().queueBulkExport(chunkX, chunkZ, radius);
                        return true;
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(ChatColor.RED + "You must provide a number");
                        return true;
                    }
                }

                sender.sendMessage(ChatColor.GREEN + String.format("The export job for your chunk (%s,%s) has been queued!", chunkX, chunkZ));

                ExportManager.getInstance().queueExport(chunkX, chunkZ);
                return true;
            } else {
                sender.sendMessage("This command can only be executed by a player.");
                return false;
            }
        });

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () ->
        {
            System.out.println("=== Processing terrain export queue! ===");
            for (int i = 0; i < ExportManager.getInstance().getQueueSize(); i++)
                ExportManager.getInstance().popQueue().run();
            System.out.println("=== Finished processing terrain export queue! ===");
        }, 6000, 6000); // 6k tick delay (5 minutes), 6k tick timer (5 minutes)

        // register events for chunk update triggers
        getServer().getPluginManager().registerEvents(new BlockEventHandler(), this);

        System.out.println("TerrainExporter enabled");
    }

    public void onDisable()
    {
        System.out.println("TerrainExporter disabled");
    }
}
