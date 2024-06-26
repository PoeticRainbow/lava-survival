package poeticrainbow.lavasurvival.game.phases;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import poeticrainbow.lavasurvival.game.LavaSurvivalConfig;
import poeticrainbow.lavasurvival.map.LavaSurvivalChunkGenerator;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
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

        center = new BlockPos((config.getX() * 16 / 2), 64, (config.getZ() * 16 / 2));
    }

    public static GameOpenProcedure open(GameOpenContext<LavaSurvivalConfig> context) {
        var config = context.config();
        var dimensionOptions = context.config().getDimensionOptions();
        var worldConfig = new RuntimeWorldConfig()
                .setDimensionType(dimensionOptions.dimensionTypeEntry())
                .setGenerator(new LavaSurvivalChunkGenerator(config, context.server()))
                .setSeed(Random.create().nextLong());

        return context.open((activity) -> {
            var gamespace = activity.getGameSpace();
            var world = gamespace.getWorlds().add(worldConfig);
            var game = new LavaSurvivalWaitingPhase(config, gamespace, world);

            GameWaitingLobby.addTo(activity, config.getPlayerConfig());

            activity.deny(GameRuleType.FALL_DAMAGE);
            activity.deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.PORTALS);
            activity.deny(GameRuleType.PVP);
            activity.deny(GameRuleType.BREAK_BLOCKS);
            activity.deny(GameRuleType.INTERACTION);
            activity.deny(GameRuleType.PICKUP_ITEMS);
            activity.deny(GameRuleType.PLACE_BLOCKS);
            activity.deny(GameRuleType.USE_ENTITIES);

            activity.listen(GamePlayerEvents.OFFER, game::onPlayerOffer);
            activity.listen(GamePlayerEvents.ADD, game::onPlayerAdd);

            activity.listen(PlayerDamageEvent.EVENT, game::onPlayerDamage);

            activity.listen(GameActivityEvents.REQUEST_START, game::onRequestStart);
        });
    }

    private GameResult onRequestStart() {
        for (ServerPlayerEntity player : gameSpace.getPlayers()) {
            player.changeGameMode(GameMode.SURVIVAL);
        }
        LavaSurvivalGracePeriod.open(this.gameSpace, this.world, this.config);
        return GameResult.ok();
    }

    private PlayerOfferResult onPlayerOffer(PlayerOffer offer) {
        ServerPlayerEntity player = offer.player();
        var safeLocation = LavaSurvivalUtil.findSafeSpot(center, player.getServerWorld());

        return offer.accept(this.world, safeLocation)
                .and(() -> {
                    player.changeGameMode(GameMode.ADVENTURE);
                });
    }

    private void onPlayerAdd(ServerPlayerEntity player) {
        var randomLoc = LavaSurvivalUtil.getRandomBlockPos(config.getX(), config.getZ());
        var safeLocation = LavaSurvivalUtil.findSafeSpot(new BlockPos(randomLoc.getX(), 64, randomLoc.getZ()), player.getServerWorld());

        if (config.getDimensionType().toString().equals("minecraft:the_nether")) {
            while (safeLocation.getY() > 127) {
                randomLoc = LavaSurvivalUtil.getRandomBlockPos(config.getX(), config.getZ());
                safeLocation = LavaSurvivalUtil.findSafeSpot(new BlockPos(randomLoc.getX(), 64, randomLoc.getZ()), player.getServerWorld());
            }
        }

        player.teleport(player.getServerWorld(), safeLocation.getX(), safeLocation.getY(), safeLocation.getZ(), 0f, 0f);
        player.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT, 0.5f, 1.0f);

        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.translatable("game.lavasurvival.desc").formatted(Formatting.RED));
        player.sendMessage(Text.literal(""));
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource damageSource, float v) {
        return ActionResult.FAIL;
    }
}
