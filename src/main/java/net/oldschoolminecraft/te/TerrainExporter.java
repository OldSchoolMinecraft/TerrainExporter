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

    public void onEnable()
    {
        getCommand("exportterrain").setExecutor((sender, command, label, args) ->
        {
            if (!(sender.hasPermission("te.saveall") || sender.isOp()))
            {
                sender.sendMessage(ChatColor.RED + "You don't have permission for this command!");
                return true;
            }

            System.out.println("=== STARTING BULK TERRAIN EXPORT ===");
            new Thread(() ->
            {
                try
                {
                    this.exportChunkedTerrainData();
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }).start();
            return true;
        });

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
                int chunkX = calculateChunkCoordinate(player.getLocation().getX());
                int chunkZ = calculateChunkCoordinate(player.getLocation().getZ());

                if (args.length > 0)
                {
                    try
                    {
                        int radius = Integer.parseInt(args[0]);
                        sender.sendMessage("Saving chunks in a radius of " + radius + " chunk(s)");
                        new Thread(() ->
                        {
                            try
                            {
                                exportChunksAroundPlayer(chunkX, chunkZ, radius);
                            } catch (Exception ex) {
                                ex.printStackTrace(System.err);
                            }
                        }).start();
                        return true;
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(ChatColor.RED + "You must provide a number");
                        return true;
                    }
                }

                sender.sendMessage("Saving your current chunk @ " + chunkX + ", " + chunkZ);

                new Thread(() ->
                {
                    try
                    {
                        exportSingleChunkTerrainData(chunkX, chunkZ);
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }
                }).start();
                return true;
            } else {
                sender.sendMessage("This command can only be executed by a player.");
                return false;
            }
        });

        getCommand("import").setExecutor(((sender, cmd, label, args) ->
        {
            if (args.length < 2)
            {
                sender.sendMessage("Usage: /import <chunkX> <chunkZ>");
                return true;
            }

            int chunkX;
            int chunkZ;

            try
            {
                chunkX = Integer.parseInt(args[0]);
                chunkZ = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                sender.sendMessage("Use numbers dummy");
                return true;
            }

            try
            {
                importSingleChunkTerrainData(chunkX, chunkZ);
            } catch (IOException e) {
                sender.sendMessage(e.getMessage());
            }

            return true;
        }));

        System.out.println("TerrainExporter enabled");
    }

    private void exportChunkedTerrainData() throws IOException, InterruptedException
    {
        for (int chunkX = -1953; chunkX <= 1953; chunkX++)
        {
            for (int chunkZ = -1953; chunkZ <= 1953; chunkZ++)
            {
                exportSingleChunkTerrainData(chunkX, chunkZ);
                Thread.sleep(250);
            }
        }
        System.out.println("=== BULK TERRAIN EXPORT COMPLETED ===");
    }

    private void exportSingleChunkTerrainData(int chunkX, int chunkZ) throws IOException
    {
        File chunkFile = new File(getDataFolder(), "chunk." + chunkX + "." + chunkZ + ".dat");
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
            Chunk chunk = getServer().getWorld("world").getChunkAt(chunkX, chunkZ);
            if (!chunk.isLoaded()) chunk.load();

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
                    int highestY = getServer().getWorld("world").getHighestBlockYAt(worldX, worldZ);
                    Block block = getServer().getWorld("world").getBlockAt(worldX, highestY - 1, worldZ);
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
    }

    private void exportChunksAroundPlayer(int centerX, int centerZ, int radius) throws IOException, InterruptedException
    {
        for (int dx = -radius; dx <= radius; dx++)
        {
            for (int dz = -radius; dz <= radius; dz++)
            {
                int chunkX = centerX + dx;
                int chunkZ = centerZ + dz;
                exportSingleChunkTerrainData(chunkX, chunkZ);
                Thread.sleep(250);
            }
        }
    }

    private void importSingleChunkTerrainData(int chunkX, int chunkZ) throws IOException
    {
        File chunkFile = new File(getDataFolder(), "chunk." + chunkX + "." + chunkZ + ".dat");
        if (!chunkFile.exists())
        {
            System.out.println("Chunk file does not exist: " + chunkX + "," + chunkZ);
            return;
        }

        try (DataInputStream dis = new DataInputStream(Files.newInputStream(chunkFile.toPath())))
        {
            // Read the starting coordinates of the chunk
            int startX = dis.readInt();
            int startZ = dis.readInt();
            if (startX != chunkX * CHUNK_SIZE || startZ != chunkZ * CHUNK_SIZE)
            {
                System.err.println("Chunk coordinates do not match: " + chunkX + "," + chunkZ);
                return;
            }

            // Loop through all blocks in the chunk
            for (int x = 0; x < CHUNK_SIZE; x++)
            {
                for (int z = 0; z < CHUNK_SIZE; z++)
                {
                    int worldX = chunkX * CHUNK_SIZE + x;
                    int worldZ = chunkZ * CHUNK_SIZE + z;

                    // Read height and block type from file
                    byte height = dis.readByte(); // Height (1 byte)
                    int blockTypeCode = dis.readInt(); // Block type (1 int)

                    // Set the block in the world
                    World world = getServer().getWorld("world");
                    int y = height; // Read height
                    if (y < 0 || y > world.getMaxHeight())
                    {
                        System.err.println("Invalid height value: " + y);
                        continue;
                    }
                    BlockRecord record = new BlockRecord(worldX, y, worldZ, blockTypeCode);
                    System.out.println("Found block: " + record);
//                    System.out.println(String.format("Imported block ID '%s' @ %s,%s,%s", blockTypeCode, worldX, y, worldZ));
                }
            }
            getLogger().info("Chunk " + chunkX + ", " + chunkZ + " imported.");
        } catch (Exception e) {
            System.err.println("Error while loading chunk (" + chunkX + "," + chunkZ + "): " + e.getMessage());
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
