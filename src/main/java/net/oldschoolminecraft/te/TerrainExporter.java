package net.oldschoolminecraft.te;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getScheduler;

public class TerrainExporter extends JavaPlugin
{
    private static final int CHUNK_SIZE = 256;

    public void onEnable()
    {
        getCommand("exportterrain").setExecutor((sender, command, label, args) ->
        {
            if (!(sender.hasPermission("te.bulk") || sender.isOp()))
            {
                sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
                return true;
            }

            System.out.println("=== STARTING BULK TERRAIN EXPORT ===");
            Bukkit.getScheduler().scheduleAsyncDelayedTask(this, this::exportChunkedTerrainData);
            return true;
        });

        // Command to export current chunk terrain data
        getCommand("exportcurrentchunk").setExecutor((sender, command, label, args) ->
        {
            if (!(sender.hasPermission("te.single") || sender.isOp()))
            {
                sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
                return true;
            }
            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                int chunkX = calculateChunkCoordinate(player.getLocation().getX());
                int chunkZ = calculateChunkCoordinate(player.getLocation().getZ());
                Bukkit.getScheduler().scheduleAsyncDelayedTask(this, () -> exportSingleChunkTerrainData(chunkX, chunkZ));
                return true;
            } else {
                sender.sendMessage("This command can only be executed by a player.");
                return false;
            }
        });

        System.out.println("TerrainExporter enabled");
    }

    private void exportChunkedTerrainData()
    {
        for (int chunkX = -1953; chunkX <= 1953; chunkX++)
            for (int chunkZ = -1953; chunkZ <= 1953; chunkZ++)
                exportSingleChunkTerrainData(chunkX, chunkZ);
        System.out.println("=== BULK TERRAIN EXPORT COMPLETED ===");
    }

    private void exportSingleChunkTerrainData(int chunkX, int chunkZ)
    {
        File chunkFile = new File(getDataFolder(), "chunk." + chunkX + "." + chunkZ + ".dat");
        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(chunkFile.toPath())))
        {
            // Write the starting coordinates of the chunk
            dos.writeInt(chunkX * CHUNK_SIZE);
            dos.writeInt(chunkZ * CHUNK_SIZE);

            // Loop through all blocks in the chunk
            for (int x = 0; x < CHUNK_SIZE; x++)
            {
                for (int z = 0; z < CHUNK_SIZE; z++)
                {
                    int worldX = chunkX * CHUNK_SIZE + x;
                    int worldZ = chunkZ * CHUNK_SIZE + z;
                    int highestY = getServer().getWorld("world").getHighestBlockYAt(worldX, worldZ);
                    Block block = getServer().getWorld("world").getBlockAt(worldX, highestY, worldZ);

                    // Get the block type code
                    byte blockTypeCode = (byte) block.getTypeId();

                    // Write height and block type to file
                    dos.writeByte(highestY); // Height (1 byte)
                    dos.writeByte(blockTypeCode); // Block type (1 byte)
                }
            }
            getLogger().info("Chunk " + chunkX + ", " + chunkZ + " exported.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    // Function to calculate chunk coordinate from block coordinate
    private int calculateChunkCoordinate(double coordinate)
    {
        return (int) Math.floor(coordinate / 16);
    }

    public void onDisable()
    {
        System.out.println("TerrainExporter disabled");
    }
}
