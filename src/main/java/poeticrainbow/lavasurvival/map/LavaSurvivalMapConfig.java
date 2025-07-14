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
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import poeticrainbow.lavasurvival.util.DimensionHolder;

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

    public DimensionOptions getDimensionOptionsFromWorldPreset() {
        return ((DimensionHolder) this.worldPreset.value()).lava_survival$getDimension(dimensionOptions).get();
    }

    public RegistryEntry<DimensionType> getDimensionTypeFromWorldPreset() {
        return ((DimensionHolder) this.worldPreset.value()).lava_survival$getDimension(dimensionOptions).get().dimensionTypeEntry();
    }

    private boolean isIncludedBiome(Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>> pair) {
        return this.excludedBiomes.isEmpty() || !this.excludedBiomes.get().contains(pair.getSecond());
    }

    public ChunkGenerator getChunkGenerator() {
        DimensionOptions dimensionOptions = getDimensionOptionsFromWorldPreset();

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

        return dimensionOptions.chunkGenerator();
    }

    public DIMENSION_TYPE getDimensionType() {
        return switch (getDimensionOptions().dimensionTypeEntry().getIdAsString()) {
            case "minecraft:the_nether" -> DIMENSION_TYPE.THE_NETHER;
            case "minecraft:the_end" -> DIMENSION_TYPE.THE_END;
            default -> DIMENSION_TYPE.OVERWORLD;
        };
    }

    public enum DIMENSION_TYPE {
        OVERWORLD,
        THE_NETHER,
        THE_END
    }
}
