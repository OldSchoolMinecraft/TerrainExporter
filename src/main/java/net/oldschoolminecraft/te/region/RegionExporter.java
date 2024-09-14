package net.oldschoolminecraft.te.region;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class RegionExporter
{
    private static final int CHUNK_SIZE = 16;
    private static final int REGION_SIZE = 16;
    private final File dataFolder;
    private final Map<String, byte[]> cache = new HashMap<>(); // Caching region data

    public RegionExporter(File dataFolder)
    {
        this.dataFolder = dataFolder;
    }

    public void exportChunk(int chunkX, int chunkZ)
    {
        int regionX = (chunkX >> 5);
        int regionZ = (chunkZ >> 5);

        String regionFileName = "region." + regionX + "." + regionZ + ".dat";
        File regionFile = new File(dataFolder, regionFileName);

        if (!regionFile.getParentFile().exists())
            regionFile.getParentFile().mkdirs();

        try
        {
            // Load the region file or create a new one if it does not exist
            byte[] regionData = cache.getOrDefault(regionFileName, new byte[REGION_SIZE * REGION_SIZE * (CHUNK_SIZE * CHUNK_SIZE * 3 + 8)]); // Adding 8 bytes for coordinates

            // Export the chunk data into the region
            ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN); // 4 bytes for chunkX, 4 bytes for chunkZ
            World world = Bukkit.getServer().getWorld("world");
            if (world == null) return; // Safety check for null world

            // Calculate the starting position in the region file
            int regionOffsetX = (chunkX % REGION_SIZE) * (CHUNK_SIZE * CHUNK_SIZE * 3 + 8); // Include coordinate space
            int regionOffsetZ = (chunkZ % REGION_SIZE) * (CHUNK_SIZE * CHUNK_SIZE * 3 + 8);
            int chunkStartIndex = (regionOffsetX * REGION_SIZE) + regionOffsetZ;

            // Write chunkX and chunkZ at the beginning of the chunk data
            buffer.putInt(chunkX);
            buffer.putInt(chunkZ);
            System.arraycopy(buffer.array(), 0, regionData, chunkStartIndex, 8);

            for (int x = 0; x < CHUNK_SIZE; x++)
            {
                for (int z = 0; z < CHUNK_SIZE; z++)
                {
                    int worldX = chunkX * CHUNK_SIZE + x;
                    int worldZ = chunkZ * CHUNK_SIZE + z;
                    int highestY = world.getHighestBlockYAt(worldX, worldZ);
                    Block block = world.getBlockAt(worldX, highestY - 1, worldZ);
                    int blockTypeCode = block.getTypeId();
                    byte data = block.getData();

                    int chunkIndex = chunkStartIndex + 8 + (x * CHUNK_SIZE * 3) + (z * 3); // Offset by 8 bytes for the coordinates
                    regionData[chunkIndex] = (byte) (highestY - 1); // Height (1 byte)
                    regionData[chunkIndex + 1] = (byte) blockTypeCode; // Block type (1 byte)
                    regionData[chunkIndex + 2] = data; // Data (1 byte)
                }
            }

            // Cache the region data
            cache.put(regionFileName, regionData);

            // Save the region file with GZIP compression
            try (FileOutputStream fos = new FileOutputStream(regionFile);
                 GZIPOutputStream gos = new GZIPOutputStream(fos))
            {
                gos.write(regionData);
            }

            // Optionally log the export
            System.out.println("Region " + regionX + ", " + regionZ + " updated with chunk " + chunkX + ", " + chunkZ + ".");

        } catch (Exception e) {
            System.err.println("Error while exporting chunk (" + chunkX + "," + chunkZ + "): " + e.getMessage());
        }
    }

    public void saveCache()
    {
        for (Map.Entry<String, byte[]> entry : cache.entrySet())
        {
            String regionFileName = entry.getKey();
            File regionFile = new File(dataFolder, regionFileName);

            try (FileOutputStream fos = new FileOutputStream(regionFile);
                 GZIPOutputStream gos = new GZIPOutputStream(fos))
            {
                gos.write(entry.getValue());
                gos.finish();
            } catch (IOException e) {
                System.err.println("Failed to save region file: " + regionFileName);
            }
        }
    }
}
