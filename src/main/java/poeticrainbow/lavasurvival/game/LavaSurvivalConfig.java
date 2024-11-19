package poeticrainbow.lavasurvival.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import poeticrainbow.lavasurvival.map.LavaSurvivalMapConfig;
import xyz.nucleoid.plasmid.api.game.common.config.PlayerLimiterConfig;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public record LavaSurvivalConfig (
        WaitingLobbyConfig lobbyConfig,
        PlayerLimiterConfig playerConfig,
        LavaSurvivalMapConfig mapConfig,
        int gracePeriod,
        int timeLimit,
        boolean armageddon
) {
    public static final MapCodec<LavaSurvivalConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        WaitingLobbyConfig.CODEC.fieldOf("lobby").forGetter(LavaSurvivalConfig::lobbyConfig),
        PlayerLimiterConfig.CODEC.fieldOf("players").forGetter(LavaSurvivalConfig::playerConfig),
        LavaSurvivalMapConfig.CODEC.fieldOf("map").forGetter(LavaSurvivalConfig::mapConfig),
        Codec.INT.fieldOf("grace_period").forGetter(LavaSurvivalConfig::gracePeriod),
        Codec.INT.fieldOf("time_limit").forGetter(LavaSurvivalConfig::timeLimit),
        Codec.BOOL.fieldOf("armageddon").forGetter(LavaSurvivalConfig::armageddon)
    ).apply(instance, LavaSurvivalConfig::new));
}
