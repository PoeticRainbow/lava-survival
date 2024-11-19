package poeticrainbow.lavasurvival.game.phases;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionOptions;
import poeticrainbow.lavasurvival.game.LavaSurvivalConfig;
import poeticrainbow.lavasurvival.map.LavaSurvivalChunkGenerator;
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

public class LavaSurvivalWaitingPhase {
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
        DimensionOptions dimensionOptions = context.config().mapConfig().getDimensionOptions();
        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setDimensionType(dimensionOptions.dimensionTypeEntry())
                .setGenerator(new LavaSurvivalChunkGenerator(config.mapConfig(), context.server()))
                .setSeed(Random.create().nextLong());

        return context.openWithWorld(worldConfig, (activity, world) -> {
            GameWaitingLobby.addTo(activity, config.lobbyConfig());

            LavaSurvivalWaitingPhase waiting = new LavaSurvivalWaitingPhase(config, activity.getGameSpace(), world);

            activity.deny(GameRuleType.FALL_DAMAGE);
            activity.deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.PORTALS);
            activity.deny(GameRuleType.PVP);
            activity.deny(GameRuleType.BREAK_BLOCKS);
            activity.deny(GameRuleType.INTERACTION);
            activity.deny(GameRuleType.PICKUP_ITEMS);
            activity.deny(GameRuleType.PLACE_BLOCKS);
            activity.deny(GameRuleType.USE_ENTITIES);

            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, waiting::onAcceptPlayers);
            activity.listen(GameActivityEvents.REQUEST_START, waiting::onRequestStart);
            activity.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> EventResult.DENY);
        });
    }

    public JoinAcceptorResult onAcceptPlayers(JoinAcceptor joinAcceptor) {
        return joinAcceptor.teleport(world, center.toCenterPos()).thenRunForEach(player -> player.changeGameMode(GameMode.ADVENTURE));
    }

    public GameResult onRequestStart() {
        gameSpace.getPlayers().forEach((player -> player.changeGameMode(GameMode.SURVIVAL)));

        LavaSurvivalGracePeriod.open(this.gameSpace, this.world, this.config);
        return GameResult.ok();
    }
}
