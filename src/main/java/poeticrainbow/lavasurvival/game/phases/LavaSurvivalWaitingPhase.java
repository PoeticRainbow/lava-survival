package poeticrainbow.lavasurvival.game.phases;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import poeticrainbow.lavasurvival.LavaSurvival;
import poeticrainbow.lavasurvival.game.LavaSurvivalConfig;
import poeticrainbow.lavasurvival.map.LavaSurvivalChunkGenerator;
import poeticrainbow.lavasurvival.util.ActivityManager;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;

import java.util.List;

public class LavaSurvivalWaitingPhase implements ActivityManager {
    private final LavaSurvivalConfig config;
    private final GameSpace gameSpace;
    private final ServerWorld world;
    private static BlockPos center = null;

    public LavaSurvivalWaitingPhase(LavaSurvivalConfig config, GameSpace gameSpace, ServerWorld world) {
        this.config = config;
        this.gameSpace = gameSpace;
        this.world = world;

        center = new BlockPos((config.mapConfig().mapWidth() * 16 / 2), 64, (config.mapConfig().mapLength() * 16 / 2));
    }

    public static GameOpenProcedure open(GameOpenContext<LavaSurvivalConfig> context) {
        LavaSurvivalConfig config = context.config();
        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setDimensionType(config.mapConfig().getDimensionTypeFromWorldPreset())
                .setGenerator(new LavaSurvivalChunkGenerator(config.mapConfig(), context.server()))
                .setSeed(Random.create().nextLong());

        return context.openWithWorld(worldConfig, (activity, world) -> {
            GameWaitingLobby.addTo(activity, config.lobbyConfig());

            LavaSurvival.LOGGER.info("Ceiling: {}", world.getDimension().hasCeiling());

            LavaSurvivalWaitingPhase waiting = new LavaSurvivalWaitingPhase(config, activity.getGameSpace(), world);

            waiting.setupAllowedActivities(activity);
            waiting.setupDeniedActivities(activity);

            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, waiting::onAcceptPlayers);
            activity.listen(GameActivityEvents.REQUEST_START, waiting::onRequestStart);
            activity.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> EventResult.DENY);
        });
    }

    public JoinAcceptorResult onAcceptPlayers(JoinAcceptor joinAcceptor) {
        return joinAcceptor.teleport(world, LavaSurvivalUtil.findSafeSpot(center, world)).thenRunForEach(player -> player.changeGameMode(GameMode.ADVENTURE));
    }

    public GameResult onRequestStart() {
        gameSpace.getPlayers().forEach((player -> player.changeGameMode(GameMode.SURVIVAL)));

        LavaSurvivalGracePeriod.open(this.gameSpace, this.world, this.config);
        return GameResult.ok();
    }

    @Override
    public List<GameRuleType> getDeniedActivities() {
        return List.of(GameRuleType.FALL_DAMAGE, GameRuleType.HUNGER,
                GameRuleType.PORTALS, GameRuleType.PVP,
                GameRuleType.BREAK_BLOCKS, GameRuleType.INTERACTION,
                GameRuleType.PICKUP_ITEMS, GameRuleType.PLACE_BLOCKS,
                GameRuleType.USE_ENTITIES, GameRuleType.CRAFTING);
    }

    @Override
    public List<GameRuleType> getAllowedActivities() {
        return List.of();
    }
}
