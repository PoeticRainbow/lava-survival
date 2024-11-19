package poeticrainbow.lavasurvival.map;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

import java.util.List;
import java.util.Optional;

public record LavaSurvivalMapConfig(
        int mapWidth,
        int mapLength,
        RegistryEntry<WorldPreset> worldPreset,
        RegistryKey<DimensionOptions> dimensionOptions,
        Optional<RegistryEntryList<Biome>> excludedBiomes
) {
    public static final Codec<LavaSurvivalMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("map_width").forGetter(LavaSurvivalMapConfig::mapWidth),
            Codec.INT.fieldOf("map_length").forGetter(LavaSurvivalMapConfig::mapLength),
            RegistryFixedCodec.of(RegistryKeys.WORLD_PRESET).fieldOf("world_preset").forGetter(LavaSurvivalMapConfig::worldPreset),
            RegistryKey.createCodec(RegistryKeys.DIMENSION).fieldOf("dimension").forGetter(LavaSurvivalMapConfig::dimensionOptions),
            RegistryCodecs.entryList(RegistryKeys.BIOME).optionalFieldOf("excluded_biomes").forGetter(LavaSurvivalMapConfig::excludedBiomes)
    ).apply(instance, LavaSurvivalMapConfig::new));

    public DimensionOptions getDimensionOptions() {
        DimensionOptionsRegistryHolder registryHolder = this.worldPreset.value().createDimensionsRegistryHolder();
        return registryHolder.dimensions().get(this.dimensionOptions);
    }

    private boolean isIncludedBiome(Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>> pair) {
        return this.excludedBiomes.isEmpty() || !this.excludedBiomes.get().contains(pair.getSecond());
    }

    public ChunkGenerator getChunkGenerator() {
        DimensionOptions dimensionOptions = getDimensionOptions();

        if (excludedBiomes().isPresent()) {
            if (dimensionOptions.chunkGenerator() instanceof NoiseChunkGenerator noiseChunkGenerator) {
                if (noiseChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource biomeSource) {
                    List<Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>>> entries = biomeSource.getBiomeEntries()
                            .getEntries()
                            .stream()
                            .filter(this::isIncludedBiome)
                            .toList();

                    MultiNoiseBiomeSource newBiomeSource = MultiNoiseBiomeSource.create(new MultiNoiseUtil.Entries<>(entries));

                    return new NoiseChunkGenerator(newBiomeSource, noiseChunkGenerator.getSettings());
                }

                throw new IllegalArgumentException("Cannot exclude biomes from unsupported biome source");
            }

            throw new IllegalArgumentException("Cannot exclude biomes from unsupported chunk generator");
        }
        return getDimensionOptions().chunkGenerator();
    }
}
