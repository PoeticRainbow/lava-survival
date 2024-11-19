package poeticrainbow.lavasurvival.game.phases;

import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.explosion.Explosion;
import poeticrainbow.lavasurvival.LavaSurvival;
import poeticrainbow.lavasurvival.game.LavaSurvivalConfig;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.world.ExplosionDetonatedEvent;

import java.util.ArrayList;
import java.util.List;

public class LavaSurvivalGracePeriod {
    private final LavaSurvivalConfig config;
    private final GameSpace gameSpace;
    private final ServerWorld world;
    private static BlockPos center;
    private static int gracePeriod;
    private static int timeElapsed;
    private static ArrayList<ServerPlayerEntity> alivePlayers;
    private final BossBarWidget bossbar;

    public LavaSurvivalGracePeriod(LavaSurvivalConfig config, GameSpace gameSpace, ServerWorld world) {
        this.config = config;
        this.gameSpace = gameSpace;
        this.world = world;

        center = new BlockPos((config.mapConfig().mapWidth() * 16 / 2), 64, (config.mapConfig().mapLength() * 16 / 2));
        gracePeriod = config.gracePeriod();
        timeElapsed = 0;
        alivePlayers = new ArrayList<ServerPlayerEntity>();

        this.bossbar = new BossBarWidget(Text.translatable("bossbar.lavasurvival.grace_period", config.gracePeriod()));
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

    private EventResult onPlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
        if (alivePlayers != null) {
            alivePlayers.remove(player);
        }
        var prefix = Text.literal("☠ ").formatted(Formatting.DARK_RED);
        if (damageSource == world.getDamageSources().lava()) {
            gameSpace.getPlayers().sendMessage(prefix.append(Text.translatable("death.lavasurvival.lava", player.getName()).formatted(Formatting.RED)));
        } else {
            gameSpace.getPlayers().sendMessage(prefix.append(Text.translatable("death.lavasurvival.other", player.getName()).formatted(Formatting.RED)));
        }
        player.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7f, LavaSurvivalUtil.randomFloat(0.7f, 1.0f));
        player.changeGameMode(GameMode.SPECTATOR);
        return EventResult.DENY;
    }

    private JoinOfferResult onPlayerOffer(JoinOffer offer) {
        //ServerPlayerEntity player = offer.player();
        //var safeLocation = LavaSurvivalUtil.findSafeSpot(center, player.getServerWorld());

        return offer.accept();
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

    private EventResult onPlayerAttack(ServerPlayerEntity attacker, Hand hand, Entity entity, EntityHitResult entityHitResult) {
        return EventResult.DENY;
    }

    private EventResult onPlayerDamage(ServerPlayerEntity player, DamageSource damageSource, float v) {
        if (damageSource.getType() == world.getDamageSources().explosion(damageSource.getSource(), damageSource.getAttacker()).getType()) {
            return EventResult.DENY;
        }
        return EventResult.ALLOW;
    }

    private EventResult onTntExplosion(Explosion explosion, List<BlockPos> blocks) {
        var entity = explosion.getEntity();
        if (entity instanceof TntEntity) {
            var world = entity.getWorld();

            for (int i = blocks.size() - 1; i >= 0; i--) {
                var blockState = world.getBlockState(blocks.get(i));
                if (!blockState.isOf(LavaSurvival.INFINITE_LAVA) && !blockState.isOf(LavaSurvival.INFINITE_LAVA_STILL)) {
                    blocks.remove(i);
                }
            }
        }
        return EventResult.ALLOW;
    }

    public int getTimeLeft() {
        return gracePeriod - timeElapsed;
    }

    public GameSpace getGameSpace() {
        return gameSpace;
    }
}
