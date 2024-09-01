package net.oldschoolminecraft.te;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftChunk;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.LinkedList;

import static org.bukkit.Bukkit.getLogger;

public class ExportManager
{
    private static final int CHUNK_SIZE = 16;
    private static final ExportManager instance = new ExportManager();

    public static ExportManager getInstance()
    {
        return instance;
    }

    private final LinkedList<Runnable> exportQueue = new LinkedList<>();
    private final LinkedList<Runnable> priorityQueue = new LinkedList<>();

    public void queueExport(int chunkX, int chunkZ)
    {
        exportQueue.add(() -> exportChunk(chunkX, chunkZ));
    }

    public void queueBulkExport(int centerX, int centerZ, int radius)
    {
        exportQueue.add(() ->
        {
            for (int dx = -radius; dx <= radius; dx++)
            {
                for (int dz = -radius; dz <= radius; dz++)
                {
                    int chunkX = centerX + dx;
                    int chunkZ = centerZ + dz;
                    exportChunk(chunkX, chunkZ);
                }
            }
        });
    }

    public void queuePriorityExport(int chunkX, int chunkZ)
    {
        priorityQueue.add(() -> exportChunk(chunkX, chunkZ));
    }

    public void queuePriorityBulkExport(int centerX, int centerZ, int radius)
    {
        priorityQueue.add(() ->
        {
            for (int dx = -radius; dx <= radius; dx++)
            {
                for (int dz = -radius; dz <= radius; dz++)
                {
                    int chunkX = centerX + dx;
                    int chunkZ = centerZ + dz;
                    exportChunk(chunkX, chunkZ);
                }
            }
        });
    }

    public int getQueueSize()
    {
        return priorityQueue.size() + exportQueue.size();
    }

    public Runnable popQueue()
    {
        if (!priorityQueue.isEmpty()) return priorityQueue.pop(); // if there are any priority tasks queued, pop them first
        else return exportQueue.pop(); // otherwise, pop the normal export queue
    }

    private void exportChunk(int chunkX, int chunkZ)
    {
        try
        {
            File chunkFile = new File(TerrainExporter.getInstance().getDataFolder(), "chunk." + chunkX + "." + chunkZ + ".dat");
            if (!chunkFile.getParentFile().exists()) chunkFile.getParentFile().mkdirs();
            if (!chunkFile.exists()) chunkFile.createNewFile();
            else {
                if (!chunkFile.delete()) chunkFile.deleteOnExit();
                if (!chunkFile.createNewFile())
                {
                    System.out.println("Failed to manage chunk file: " + chunkX + "," + chunkZ);
                    return;
                }
            }
            try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(chunkFile.toPath())))
            {
                Chunk chunk = Bukkit.getServer().getWorld("world").getChunkAt(chunkX, chunkZ);
                if (!chunk.isLoaded()) chunk.load(true);

                ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);

                buffer.putInt(chunkX * CHUNK_SIZE);
                dos.write(buffer.array());

                buffer.clear();
                buffer.putInt(chunkZ * CHUNK_SIZE);
                dos.write(buffer.array());

                for (int x = 0; x < CHUNK_SIZE; x++)
                {
                    for (int z = 0; z < CHUNK_SIZE; z++)
                    {
                        int worldX = chunkX * CHUNK_SIZE + x;
                        int worldZ = chunkZ * CHUNK_SIZE + z;
                        int highestY = Bukkit.getServer().getWorld("world").getHighestBlockYAt(worldX, worldZ);
                        Block block = Bukkit.getServer().getWorld("world").getBlockAt(worldX, highestY - 1, worldZ);
                        int blockTypeCode = block.getTypeId();

                        dos.writeByte(highestY); // Height (1 byte)
                        buffer.clear();
                        buffer.putInt(blockTypeCode);
                        dos.write(buffer.array()); // Block type (4 bytes)
                    }
                }
                getLogger().info("Chunk " + chunkX + ", " + chunkZ + " exported.");
            } catch (Exception e) {
                System.err.println("Error while saving chunk (" + chunkX + "," + chunkZ + "): " + e.getMessage());
            }
        } catch (Exception ex) {
            System.err.println("Error while saving chunk (" + chunkX + "," + chunkZ + "): " + ex.getMessage());
        }
    }
}
