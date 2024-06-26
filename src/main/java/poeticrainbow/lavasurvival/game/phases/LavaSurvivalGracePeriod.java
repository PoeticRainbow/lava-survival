package poeticrainbow.lavasurvival.game.phases;

import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
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
import net.minecraft.world.explosion.Explosion;
import poeticrainbow.lavasurvival.LavaSurvival;
import poeticrainbow.lavasurvival.game.LavaSurvivalConfig;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.world.ExplosionDetonatedEvent;

import java.util.ArrayList;

public class LavaSurvivalGracePeriod {
    private final LavaSurvivalConfig config;
    private final GameSpace gameSpace;
    private final ServerWorld world;
    private static BlockPos center;
    private static int gracePeriod;
    private static int timeElapsed;
    private static ArrayList<ServerPlayerEntity> alivePlayers;
    private BossBarWidget bossbar;

    public LavaSurvivalGracePeriod(LavaSurvivalConfig config, GameSpace gameSpace, ServerWorld world) {
        this.config = config;
        this.gameSpace = gameSpace;
        this.world = world;

        center = new BlockPos((config.getX() * 16 / 2), 64, (config.getZ() * 16 / 2));
        gracePeriod = config.getGracePeriod();
        timeElapsed = 0;
        alivePlayers = new ArrayList<ServerPlayerEntity>();

        this.bossbar = new BossBarWidget(Text.translatable("bossbar.lavasurvival.grace_period", config.getGracePeriod()));
        this.bossbar.setStyle(BossBar.Color.RED, BossBar.Style.PROGRESS);
    }

    public static void open(GameSpace gameSpace, ServerWorld world, LavaSurvivalConfig config) {
        gameSpace.setActivity(activity -> {
            LavaSurvivalGracePeriod phase = new LavaSurvivalGracePeriod(config, gameSpace, world);

            activity.listen(GamePlayerEvents.OFFER, phase::onPlayerOffer);
            activity.listen(GamePlayerEvents.ADD, phase::onPlayerAdd);
            activity.listen(GamePlayerEvents.REMOVE, phase::onPlayerRemove);
            activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
            activity.listen(PlayerAttackEntityEvent.EVENT, phase::onPlayerAttack);
            activity.listen(PlayerDamageEvent.EVENT, phase::onPlayerDamage);
            activity.listen(GameActivityEvents.TICK, phase::onTick);
            activity.listen(ExplosionDetonatedEvent.EVENT, phase::onTntExplosion);

            activity.deny(GameRuleType.SATURATED_REGENERATION);
            activity.deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.PORTALS);
        });
    }

    private void onTick() {
        var currentTime = gameSpace.getTime();
        var players = gameSpace.getPlayers();
        if (currentTime % 20 == 0) {
            bossbar.setTitle(Text.translatable("bossbar.lavasurvival.grace_period", getTimeLeft()));
            bossbar.setProgress((float) getTimeLeft() / gracePeriod);
            if (timeElapsed > gracePeriod) {
                LavaSurvivalActive.open(this.gameSpace, this.world, this.config);
                players.sendMessage(Text.literal(""));
                players.sendMessage(Text.translatable("message.lavasurvival.grace_period_end"));
                players.sendMessage(Text.literal(""));
            }
            timeElapsed++;
        }
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
        if (alivePlayers != null) {
            alivePlayers.remove(player);
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
        bossbar.addPlayer(player);

        // Don't add the player if they are in spectator or creative
        if (alivePlayers != null && !player.isSpectator() && !player.isCreative()) {
            player.getInventory().setStack(0, new ItemStack(LavaSurvival.BLOCK_MENU_ITEM, 1));
            player.getInventory().setStack(1, LavaSurvivalUtil.createUnbreakableTool(Items.DIAMOND_PICKAXE));
            player.getInventory().setStack(2, LavaSurvivalUtil.createUnbreakableTool(Items.GOLDEN_SHOVEL));
            player.getInventory().setStack(3, LavaSurvivalUtil.createUnbreakableTool(Items.GOLDEN_AXE));

            alivePlayers.add(player);
        } else {
            player.sendMessage(Text.translatable("message.lavasurvival.spectate"));
        }
    }

    private void onPlayerRemove(ServerPlayerEntity player) {
        alivePlayers.remove(player);
        bossbar.removePlayer(player);
    }

    private ActionResult onPlayerAttack(ServerPlayerEntity attacker, Hand hand, Entity entity, EntityHitResult entityHitResult) {
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource damageSource, float v) {
        if (damageSource.getType() == world.getDamageSources().explosion(damageSource.getSource(), damageSource.getAttacker()).getType()) {
            return ActionResult.FAIL;
        }
        return ActionResult.SUCCESS;
    }

    private void onTntExplosion(Explosion explosion, boolean b) {
        var entity = explosion.getEntity();
        if (entity instanceof TntEntity) {
            var blocks = explosion.getAffectedBlocks();
            var world = entity.getWorld();

            for (int i = blocks.size() - 1; i >= 0; i--) {
                var blockState = world.getBlockState(blocks.get(i));
                if (!blockState.isOf(LavaSurvival.INFINITE_LAVA) && !blockState.isOf(LavaSurvival.INFINITE_LAVA_STILL)) {
                    blocks.remove(i);
                }
            }
        }
    }

    public int getTimeLeft() {
        return gracePeriod - timeElapsed;
    }

    public GameSpace getGameSpace() {
        return gameSpace;
    }
}
