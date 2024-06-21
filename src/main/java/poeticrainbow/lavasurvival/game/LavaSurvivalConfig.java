package poeticrainbow.lavasurvival.game;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

import java.util.List;
import java.util.Optional;

public final class LavaSurvivalConfig {
    public static final Codec<LavaSurvivalConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(LavaSurvivalConfig::getPlayerConfig),
            Codec.INT.fieldOf("grace_period").forGetter(LavaSurvivalConfig::getGracePeriod),
            Codec.INT.fieldOf("time_limit").forGetter(LavaSurvivalConfig::getTimeLimit),
            Codec.INT.fieldOf("x_size_chunks").forGetter(LavaSurvivalConfig::getX),
            Codec.INT.fieldOf("z_size_chunks").forGetter(LavaSurvivalConfig::getZ),
            Codec.BOOL.fieldOf("armageddon").forGetter(LavaSurvivalConfig::getArmageddon),
            RegistryFixedCodec.of(RegistryKeys.WORLD_PRESET).fieldOf("preset").forGetter(config -> config.worldPreset),
            RegistryKey.createCodec(RegistryKeys.DIMENSION).optionalFieldOf("dimension_options", DimensionOptions.OVERWORLD).forGetter(config -> config.dimensionOptions),
            RegistryCodecs.entryList(RegistryKeys.BIOME).optionalFieldOf("excluded_biomes").forGetter(config -> config.excludedBiomes)
    ).apply(instance, LavaSurvivalConfig::new));

    private final PlayerConfig playerConfig;
    private final int gracePeriod;
    private final int timeLimit;

    private final int xBorderSize;
    private final int zBorderSize;

    private final boolean armageddon;

    private final RegistryEntry<WorldPreset> worldPreset;
    private final RegistryKey<DimensionOptions> dimensionOptions;
    private final Optional<RegistryEntryList<Biome>> excludedBiomes;
    private ChunkGenerator chunkGenerator;

    public LavaSurvivalConfig(PlayerConfig playerConfig, int gracePeriod, int timeLimit, int xBorderSize, int zBorderSize, boolean armageddon,
                              RegistryEntry<WorldPreset> worldPreset, RegistryKey<DimensionOptions> dimensionOptions,
                              Optional<RegistryEntryList<Biome>> excludedBiomes) {
        this.playerConfig = playerConfig;
        this.gracePeriod = gracePeriod;
        this.timeLimit = timeLimit;
        this.xBorderSize = xBorderSize;
        this.zBorderSize = zBorderSize;
        this.worldPreset = worldPreset;
        this.dimensionOptions = dimensionOptions;
        this.excludedBiomes = excludedBiomes;
        this.armageddon = armageddon;
    }

    public ChunkGenerator getChunkGenerator() {
        if (this.chunkGenerator == null) {
            this.chunkGenerator = this.createChunkGenerator();
        }

        return this.chunkGenerator;
    }

    public DimensionOptions getDimensionOptions() {
        DimensionOptionsRegistryHolder registryHolder = this.worldPreset.value().createDimensionsRegistryHolder();
        return registryHolder.dimensions().get(this.dimensionOptions);
    }

    private ChunkGenerator createChunkGenerator() {
        DimensionOptions dimensionOptions = this.getDimensionOptions();

        if (this.excludedBiomes.isPresent()) {
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

    private boolean isIncludedBiome(Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>> pair) {
        return this.excludedBiomes.isEmpty() || !this.excludedBiomes.get().contains(pair.getSecond());
    }

    public PlayerConfig getPlayerConfig() {
        return this.playerConfig;
    }

    public int getX() {
        return this.xBorderSize;
    }

    public int getZ() {
        return this.zBorderSize;
    }

    public Integer getGracePeriod() {
        return this.gracePeriod;
    }

    public int getTimeLimit() {
        return this.timeLimit;
    }

    public Identifier getDimensionType() {
        return this.dimensionOptions.getValue();
    }

    public Boolean getArmageddon() {
        return this.armageddon;
    }
}
