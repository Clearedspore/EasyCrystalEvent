package me.clearedspore.easyCrystalEvent.generation;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;


public class SandstoneChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);


        double scale = 0.05;
        int baseHeight = 40;
        int maxVariation = 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, -1, z, Material.BEDROCK);
                chunkData.setBlock(x, 0, z, Material.BEDROCK);


                double noiseValue = noise(chunkX * 16 + x, chunkZ * 16 + z, scale);
                int heightVariation = (int) (noiseValue * maxVariation);
                int height = baseHeight + heightVariation;

                for (int y = 1; y < height; y++) {
                    chunkData.setBlock(x, y, z, Material.SANDSTONE);
                }


                chunkData.setBlock(x, height, z, Material.SAND);
                chunkData.setBlock(x, height + 1, z, Material.SAND);
                chunkData.setBlock(x, height + 2, z, Material.SAND);
            }
        }

        return chunkData;
    }

    // Simple noise function for demonstration purposes
    private double noise(int x, int z, double scale) {
        return Math.sin(x * scale) * Math.cos(z * scale);
    }
}