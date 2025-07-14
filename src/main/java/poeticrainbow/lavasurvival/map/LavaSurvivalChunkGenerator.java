package poeticrainbow.lavasurvival.map;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;
import poeticrainbow.lavasurvival.LavaSurvival;
import xyz.nucleoid.fantasy.util.ChunkGeneratorSettingsProvider;
import xyz.nucleoid.plasmid.api.game.world.generator.GameChunkGenerator;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.block.BarrierBlock.WATERLOGGED;

public class LavaSurvivalChunkGenerator extends GameChunkGenerator implements ChunkGeneratorSettingsProvider {
    private final LavaSurvivalMapConfig config;
    private final ChunkGenerator chunkGenerator;

    public LavaSurvivalChunkGenerator(LavaSurvivalMapConfig config, MinecraftServer server) {
        super(config.getChunkGenerator().getBiomeSource());
        this.config = config;
        this.chunkGenerator = config.getChunkGenerator();

        if (chunkGenerator instanceof FlatChunkGenerator flatGenerator) {
            flatGenerator.getConfig().getLayerBlocks().forEach((layer) -> LavaSurvival.LOGGER.info("Layer: {}", layer));
        }
    }

    // Returns true if the chunk is in the playable region
    private boolean isChunkWithinArea(Chunk chunk) {
        var chunkPos = chunk.getPos();
        return chunkPos.x >= 0 && chunkPos.z >= 0 && chunkPos.x < this.config.mapWidth() && chunkPos.z < this.config.mapLength();
    }

    // Returns true if the chunk is within the water border region
    private boolean isChunkWithinBorderArea(Chunk chunk, int borderWidth) {
        var chunkPos = chunk.getPos();
        return chunkPos.x >= -borderWidth && chunkPos.z >= -borderWidth && chunkPos.x < this.config.mapWidth() + borderWidth && chunkPos.z < this.config.mapLength() + borderWidth;
    }

    @Override
    public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        if (this.isChunkWithinArea(chunk)) {
            return this.chunkGenerator.populateBiomes(noiseConfig, blender, structureAccessor, chunk);
        }
        return super.populateBiomes(noiseConfig, blender, structureAccessor, chunk);
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        if (this.isChunkWithinArea(chunk)) {
            return this.chunkGenerator.populateNoise(blender, noiseConfig, structureAccessor, chunk);
        }
        return super.populateNoise(blender, noiseConfig, structureAccessor, chunk);
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

                    LavaSurvivalMapConfig.DIMENSION_TYPE dimensionType = config.getDimensionType();

                    if (dimensionType == LavaSurvivalMapConfig.DIMENSION_TYPE.THE_NETHER) {
                        preSurfaceBlock = Blocks.NETHERRACK.getDefaultState();
                        var startY = 76;
                        for (int y = startY; y < startY + 6; y++) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
                        }
                    }
                    if (dimensionType == LavaSurvivalMapConfig.DIMENSION_TYPE.THE_END) {
                        preSurfaceBlock = Blocks.END_STONE.getDefaultState();
                    }


                    if (chunkGenerator instanceof FlatChunkGenerator) {
                        region.setBlockState(new BlockPos(x, 61, z), Blocks.DIRT.getDefaultState(), 3);
                        region.setBlockState(new BlockPos(x, 62, z), Blocks.GRASS_BLOCK.getDefaultState(), 3);
                    } else {
                        region.setBlockState(new BlockPos(x, 61, z), preSurfaceBlock, 3);
                        region.setBlockState(new BlockPos(x, 62, z), preSurfaceBlock, 3);
                    }
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

        // Determine default liquid for dimension type
        BlockState outerLiquid = Blocks.BARRIER.getDefaultState().with(WATERLOGGED, true);
        if (config.getDimensionType() == LavaSurvivalMapConfig.DIMENSION_TYPE.THE_NETHER) {
            outerLiquid = Blocks.LAVA.getDefaultState();
        }

        // Directly a border chunk
        if (this.isChunkWithinBorderArea(chunk, 1)) {
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = 59; y < 320; y++) {
                        if (y <= 60) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.getDefaultState(), 3);
                        } else if (y < 63) {
                            region.setBlockState(new BlockPos(x, y, z), outerLiquid, 3);
                        } else {
                            BlockState border = Blocks.BARRIER.getDefaultState();

                            // If cave style
                            if (!config.getDimensionTypeFromWorldPreset().value().hasSkyLight() && config.getDimensionTypeFromWorldPreset().value().hasCeiling()) {
                                border = Blocks.BEDROCK.getDefaultState();
                            }
                            region.setBlockState(new BlockPos(x, y, z), border, 3);
                        }
                    }
                }
            }
            return;
        }

        // Classic outer water chunk
        if (this.isChunkWithinBorderArea(chunk, 16)) {
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = 59; y < 63; y++) {
                        if (y <= 60) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.getDefaultState(), 3);
                        } else {
                            region.setBlockState(new BlockPos(x, y, z), outerLiquid, 3);
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
                    chunk.setBlockState(new BlockPos(x, 319, z), Blocks.BARRIER.getDefaultState(), 0);
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
