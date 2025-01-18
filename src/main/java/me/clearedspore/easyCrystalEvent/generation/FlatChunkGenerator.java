package me.clearedspore.easyCrystalEvent.generation;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class FlatChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, -1, z, Material.BEDROCK);
                chunkData.setBlock(x, 0, z, Material.BEDROCK);
                for (int y = 1; y <= 4; y++) {
                    chunkData.setBlock(x, y, z, Material.DIRT);
                }
                chunkData.setBlock(x, 5, z, Material.GRASS_BLOCK);
            }
        }

        return chunkData;
    }
}