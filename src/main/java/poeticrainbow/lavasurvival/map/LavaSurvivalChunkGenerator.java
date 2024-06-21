package poeticrainbow.lavasurvival.map;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import poeticrainbow.lavasurvival.game.LavaSurvivalConfig;
import xyz.nucleoid.fantasy.util.ChunkGeneratorSettingsProvider;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static net.minecraft.block.BarrierBlock.WATERLOGGED;

public class LavaSurvivalChunkGenerator extends GameChunkGenerator implements ChunkGeneratorSettingsProvider {
    private final LavaSurvivalConfig config;
    private final ChunkGenerator chunkGenerator;

    public LavaSurvivalChunkGenerator(LavaSurvivalConfig config, MinecraftServer server) {
        super(config.getChunkGenerator().getBiomeSource());
        this.config = config;
        this.chunkGenerator = config.getChunkGenerator();
    }



    private boolean isChunkWithinArea(Chunk chunk) {
        var chunkPos = chunk.getPos();
        return chunkPos.x >= 0 && chunkPos.z >= 0 && chunkPos.x < this.config.getX() && chunkPos.z < this.config.getZ();
    }

    private boolean isChunkWithinBorderArea(Chunk chunk, int borderWidth) {
        var chunkPos = chunk.getPos();
        return chunkPos.x >= -borderWidth && chunkPos.z >= -borderWidth && chunkPos.x < this.config.getX() + borderWidth && chunkPos.z < this.config.getZ() + borderWidth;
    }

    @Override
    public CompletableFuture<Chunk> populateBiomes(Executor executor, NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        if (this.isChunkWithinArea(chunk)) {
            return this.chunkGenerator.populateBiomes(executor, noiseConfig, blender, structureAccessor, chunk);
        }
        return super.populateBiomes(executor, noiseConfig, blender, structureAccessor, chunk);
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        if (this.isChunkWithinArea(chunk)) {
            return this.chunkGenerator.populateNoise(executor, blender, noiseConfig, structureAccessor, chunk);
        }
        return super.populateNoise(executor, blender, noiseConfig, structureAccessor, chunk);
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        // Normal Minecraft terrain with a layer of bedrock
        if (this.isChunkWithinArea(chunk)) {
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    // Ensure there is no water worlds... scuffed
                    var preSurfaceBlock = Blocks.STONE.getDefaultState();

                    if (config.getDimensionType().toString().equals("minecraft:the_nether")) {
                        preSurfaceBlock = Blocks.NETHERRACK.getDefaultState();
                        var startY = 76;
                        for (int y = startY; y < startY + 6; y++) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
                        }
                    }
                    if (config.getDimensionType().toString().equals("minecraft:the_end")) {
                        preSurfaceBlock = Blocks.END_STONE.getDefaultState();
                    }

                    region.setBlockState(new BlockPos(x, 61, z), preSurfaceBlock, 3);
                    region.setBlockState(new BlockPos(x, 62, z), preSurfaceBlock, 3);
                }
            }

            // Surface decoration
            this.chunkGenerator.buildSurface(region, structures, noiseConfig, chunk);

            // Bedrock Layer
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    region.setBlockState(new BlockPos(x, 59, z), Blocks.BEDROCK.getDefaultState(), 3);
                    region.setBlockState(new BlockPos(x, 60, z), Blocks.BEDROCK.getDefaultState(), 3);

                    // Loop through Y to clear out underground
                    for (int y = -64; y < 59; y++) {
                        region.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
            return;
        }

        // Directly a border chunk
        if (this.isChunkWithinBorderArea(chunk, 1)) {
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = 59; y < 320; y++) {
                        if (y <= 60) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.getDefaultState(), 3);
                        } else if (y < 63) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.BARRIER.getDefaultState().with(WATERLOGGED, true), 3);
                        } else {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.BARRIER.getDefaultState(), 3);
                        }
                    }
                }
            }
            return;
        }

        //Outer render chunk
        if (this.isChunkWithinBorderArea(chunk, 6)) {
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = 59; y < 63; y++) {
                        if (y <= 60) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.getDefaultState(), 3);
                        } else {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.WATER.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public BiomeSource getBiomeSource() {
        return this.chunkGenerator.getBiomeSource();
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        if (this.isChunkWithinArea(chunk)) {
            this.chunkGenerator.generateFeatures(world, chunk, structureAccessor);
            int startX = chunk.getPos().getStartX();
            int startZ = chunk.getPos().getStartZ();


            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    chunk.setBlockState(new BlockPos(x, 319, z), Blocks.BARRIER.getDefaultState(), false);
                }
            }
        }
    }

    @Override
    public ChunkGeneratorSettings getSettings() {
        if (this.chunkGenerator instanceof NoiseChunkGenerator noiseChunkGenerator) {
            return noiseChunkGenerator.getSettings().value();
        }
        return null;
    }
}
