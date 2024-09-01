package net.oldschoolminecraft.te;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.io.DataOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.bukkit.Bukkit.getLogger;

public class Utils
{
    public static int calculateChunkCoordinate(double coordinate)
    {
        return (int) Math.floor(coordinate / 16);
    }

    public static void exportChunk(int chunkX, int chunkZ)
    {
        try
        {
            int CHUNK_SIZE = 16;
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
                        byte data = block.getData();

                        dos.writeByte(highestY); // Height (1 byte)
                        dos.writeByte(blockTypeCode);
                        dos.writeByte(data);
                    }
                }
//                getLogger().info("Chunk " + chunkX + ", " + chunkZ + " exported.");
            } catch (Exception e) {
                System.err.println("Error while saving chunk (" + chunkX + "," + chunkZ + "): " + e.getMessage());
            }
        } catch (Exception ex) {
            System.err.println("Error while saving chunk (" + chunkX + "," + chunkZ + "): " + ex.getMessage());
        }
    }

    public static String buildHash(Object... values)
    {
        // StringBuilder to accumulate the concatenated string
        StringBuilder concatenatedString = new StringBuilder();

        // Append each value to the StringBuilder
        for (Object value : values)
            concatenatedString.append(value.toString());

        // Convert the concatenated string to bytes
        byte[] bytes = concatenatedString.toString().getBytes(StandardCharsets.UTF_8);

        try
        {
            // Create a SHA-1 MessageDigest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            // Compute the hash bytes
            byte[] hashBytes = digest.digest(bytes);

            // Convert the hash bytes to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes)
            {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            // Return the hexadecimal string representation of the SHA-1 hash
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }
}
