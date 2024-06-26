package poeticrainbow.lavasurvival.game.phases;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import poeticrainbow.lavasurvival.LavaSurvival;
import poeticrainbow.lavasurvival.LavaSurvivalDataGenerator;
import poeticrainbow.lavasurvival.game.LavaSurvivalConfig;
import poeticrainbow.lavasurvival.game.LavaSurvivalWidgets;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class LavaSurvivalActive {
    private final LavaSurvivalConfig config;
    private final GameSpace gameSpace;
    private final ServerWorld world;
    private static BlockPos center;
    private static int timeElapsed;
    private static ArrayList<ServerPlayerEntity> alivePlayers;
    private static HashMap<UUID, Integer> playerScores;
    private static LavaSurvivalWidgets widgets;
    private int timeUntilEnd = -1;

    public LavaSurvivalActive(LavaSurvivalConfig config, GameSpace gameSpace, ServerWorld world) {
        this.config = config;
        this.gameSpace = gameSpace;
        this.world = world;

        center = new BlockPos((config.getX() * 16 / 2), 64, (config.getZ() * 16 / 2));
        timeElapsed = 0;
        widgets = new LavaSurvivalWidgets(this);
        alivePlayers = new ArrayList<ServerPlayerEntity>();
        playerScores = new HashMap<UUID, Integer>();

        widgets = new LavaSurvivalWidgets(this);

        widgets.updateWidgets(playerScores);
    }

    public static void open(GameSpace gameSpace, ServerWorld world, LavaSurvivalConfig config) {
        gameSpace.setActivity(activity -> {
            LavaSurvivalActive phase = new LavaSurvivalActive(config, gameSpace, world);

            activity.listen(GamePlayerEvents.OFFER, phase::onPlayerOffer);
            activity.listen(GamePlayerEvents.ADD, phase::onPlayerAdd);
            activity.listen(GamePlayerEvents.REMOVE, phase::onPlayerRemove);
            activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
            activity.listen(PlayerAttackEntityEvent.EVENT, phase::onPlayerAttack);
            activity.listen(PlayerDamageEvent.EVENT, phase::onPlayerDamage);
            activity.listen(GameActivityEvents.TICK, phase::onTick);
            activity.listen(GameActivityEvents.DESTROY, phase::onGameClose);

            activity.listen(BlockPlaceEvent.AFTER, phase::onBlockPlace);

            activity.allow(GameRuleType.UNSTABLE_TNT);

            activity.deny(GameRuleType.SATURATED_REGENERATION);
            activity.deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.PORTALS);

            spawnLava(world, config);
            gameSpace.getPlayers().playSound(SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.MASTER, 1.0f, 0.5f);
        });
    }

    private void onTick() {
        var currentTime = gameSpace.getTime();
        if (currentTime % 20 == 0) {
            // End game behavior
            if (timeUntilEnd > -1) {
                if (timeUntilEnd > 0) {
                    if (!alivePlayers.isEmpty()) {
                        for (ServerPlayerEntity alivePlayer : alivePlayers) {
                            var pos = alivePlayer.getPos();
                            world.spawnEntity(new FireworkRocketEntity(world, pos.getX(), pos.getY(), pos.getZ(), Items.FIREWORK_ROCKET.getDefaultStack()));
                        }
                    }
                    timeUntilEnd--;
                    return;
                } else {
                    gameSpace.close(GameCloseReason.FINISHED);
                }
            }
            // End the game if al the players are dead
            if (alivePlayers.isEmpty() && timeUntilEnd == -1) {
                // End game
                var players = gameSpace.getPlayers();
                players.sendMessage(Text.literal(""));
                players.sendMessage(Text.translatable("message.lavasurvival.lose").formatted(Formatting.RED, Formatting.BOLD));
                players.sendMessage(Text.literal(""));
                timeUntilEnd = 5;
            }
            // End the game if the time limit has been exceeded: win!
            if (timeElapsed >= config.getTimeLimit() && timeUntilEnd == -1) {
                var players = gameSpace.getPlayers();
                players.sendMessage(Text.literal(""));
                players.sendMessage(Text.translatable("message.lavasurvival.win").formatted(Formatting.GOLD, Formatting.BOLD));
                var winners = Text.literal("");
                for (ServerPlayerEntity alivePlayer : alivePlayers) {
                    winners.append(alivePlayer.getDisplayName()).append(" ");
                }
                players.sendMessage(winners);
                players.sendMessage(Text.literal(""));
                timeUntilEnd = 5;
            }
            // Increase scores with time and update timer and sidebar
            for (ServerPlayerEntity player : alivePlayers) {
                if (playerScores.containsKey(player.getUuid())) {
                    var score = playerScores.get(player.getUuid());
                    playerScores.replace(player.getUuid(), score + 1);
                }
            }
            widgets.updateWidgets(playerScores);

            // Armageddon mode >:)
            if (config.getArmageddon() && currentTime % 100 == 0) {
                spawnLava(world, config);
            }

            timeElapsed++;
        }
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
        if (alivePlayers != null && alivePlayers.contains(player)) {
            alivePlayers.remove(player);
            playerScores.remove(player.getUuid());
        }
        var prefix = Text.literal("☠ ").formatted(Formatting.DARK_RED);
        if (damageSource == world.getDamageSources().lava()) {
            gameSpace.getPlayers().sendMessage(prefix.append(Text.translatable("death.lavasurvival.lava", player.getName()).formatted(Formatting.RED)));
        } else {
            gameSpace.getPlayers().sendMessage(prefix.append(Text.translatable("death.lavasurvival.other", player.getName()).formatted(Formatting.RED)));
        }
        player.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.7f, LavaSurvivalUtil.randomFloat(0.7f, 1.0f));
        player.changeGameMode(GameMode.SPECTATOR);
        return ActionResult.FAIL;
    }

    private PlayerOfferResult onPlayerOffer(PlayerOffer offer) {
        ServerPlayerEntity player = offer.player();
        var safeLocation = LavaSurvivalUtil.findSafeSpot(center, player.getServerWorld());

        return offer.accept(this.world, safeLocation)
                .and(() -> {
                    player.changeGameMode(GameMode.SPECTATOR);
                });
    }

    private void onPlayerAdd(ServerPlayerEntity player) {
        // Don't add the player if they are in spectator or creative
        if (alivePlayers != null && !player.isSpectator() && !player.isCreative()) {
            alivePlayers.add(player);
            playerScores.put(player.getUuid(), 0);
        } else {
            player.sendMessage(Text.translatable("message.lavasurvival.spectate"));
        }

        widgets.addPlayer(player);
    }

    private void onPlayerRemove(ServerPlayerEntity player) {
        alivePlayers.remove(player);
        playerScores.remove(player.getUuid());

        widgets.removePlayer(player);

        widgets.updateWidgets(playerScores);
    }

    private ActionResult onPlayerAttack(ServerPlayerEntity attacker, Hand hand, Entity entity, EntityHitResult entityHitResult) {
        if (!attacker.isSpectator() && entity instanceof ServerPlayerEntity hitPlayer) {
            hitPlayer.damage(world.getDamageSources().playerAttack(attacker), 0.1f);
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource damageSource, float v) {
        if (damageSource.getType() == world.getDamageSources().explosion(damageSource.getSource(), damageSource.getAttacker()).getType()) {
            return ActionResult.FAIL;
        }
        return ActionResult.SUCCESS;
    }

    private void onGameClose(GameCloseReason gameCloseReason) {
        for (ServerPlayerEntity player : alivePlayers) {
            widgets.removePlayer(player);
        }
        widgets.destroy();
    }

    private void onBlockPlace(ServerPlayerEntity player, ServerWorld serverWorld, BlockPos pos, BlockState blockState) {
        var scoreChange = 0;

        if (blockState.isIn(LavaSurvivalDataGenerator.ModBlockTagGenerator.ONE_POINT_BLOCKS)) {
            scoreChange = 1;
        } else if (blockState.isIn(LavaSurvivalDataGenerator.ModBlockTagGenerator.THREE_POINT_BLOCKS)) {
            scoreChange = 3;
        } else if (blockState.isIn(LavaSurvivalDataGenerator.ModBlockTagGenerator.FIVE_POINT_BLOCKS)) {
            scoreChange = 5;
        }

//        player.sendMessage(Text.literal("+" + scoreChange), true);
        changeScore(player, scoreChange);
    }

    private void changeScore(ServerPlayerEntity player, Integer integer) {
        var uuid = player.getUuid();
        var currentScore = playerScores.get(uuid);
        var newScore = currentScore + integer;

        if (newScore < 0) {
            newScore = 0;
        }

        playerScores.replace(uuid, newScore);
    }

    private static void spawnLava(ServerWorld world, LavaSurvivalConfig config) {
        var location = LavaSurvivalUtil.getRandomBlockPos(config.getX(), config.getZ());
        var safeLavaLocation = LavaSurvivalUtil.getTopBlock(location, world);

        for (var i = 16; i >= 0; i--) {
            if (world.getBlockState(safeLavaLocation.up(i)).isAir()) {
                world.setBlockState(safeLavaLocation.up(i), LavaSurvival.INFINITE_LAVA.getDefaultState());
                return;
            }
        }
        world.setBlockState(safeLavaLocation, LavaSurvival.INFINITE_LAVA.getDefaultState());
    }

    // Getters
    public long getTimeLeft() {
        // in seconds
        return config.getTimeLimit() - timeElapsed;
    }

    public float getGameProgress() {
        return (float) (config.getTimeLimit() - timeElapsed) / config.getTimeLimit();
    }

    public GameSpace getGameSpace() {
        return gameSpace;
    }
}
