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
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
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
                    String dimensionType = config.dimensionOptions().getValue().toString();

                    if (dimensionType.equals("minecraft:the_nether")) {
                        preSurfaceBlock = Blocks.NETHERRACK.getDefaultState();
                        var startY = 76;
                        for (int y = startY; y < startY + 6; y++) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
                        }
                    }
                    if (dimensionType.equals("minecraft:the_end")) {
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
            BlockState outerLiquid = Blocks.BARRIER.getDefaultState().with(WATERLOGGED, true);
            if (config.getDimensionOptions().dimensionTypeEntry().getType().toString().equals("minecraft:the_nether")) {
                outerLiquid = Blocks.LAVA.getDefaultState();
            }
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = 59; y < 320; y++) {
                        if (y <= 60) {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.BEDROCK.getDefaultState(), 3);
                        } else if (y < 63) {
                            region.setBlockState(new BlockPos(x, y, z), outerLiquid, 3);
                        } else {
                            region.setBlockState(new BlockPos(x, y, z), Blocks.BARRIER.getDefaultState(), 3);
                        }
                    }
                }
            }
            return;
        }

        // Classic outer water chunk
        if (this.isChunkWithinBorderArea(chunk, 16)) {
            BlockState outerLiquid = Blocks.WATER.getDefaultState();
            if (config.getDimensionOptions().dimensionTypeEntry().getType().toString().equals("minecraft:the_nether")) {
                outerLiquid = Blocks.LAVA.getDefaultState();
            }
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
