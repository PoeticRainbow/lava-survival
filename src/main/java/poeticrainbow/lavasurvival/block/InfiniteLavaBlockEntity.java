package poeticrainbow.lavasurvival.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import poeticrainbow.lavasurvival.LavaSurvival;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

public class InfiniteLavaBlockEntity extends BlockEntity {
    private static int spreadTime;

    public InfiniteLavaBlockEntity(BlockPos pos, BlockState state) {
        super(LavaSurvival.INFINITE_LAVA_BLOCK_ENTITY, pos, state);
        spreadTime = 30;
    }

    public InfiniteLavaBlockEntity(BlockPos pos, BlockState state, int ticks) {
        super(LavaSurvival.INFINITE_LAVA_BLOCK_ENTITY, pos, state);
        spreadTime = ticks;
    }

    public static void spreadLava(ServerWorld world, BlockPos pos) {
        if (pos.getY() > 192) {
            return;
        }
        var targetBlockState = world.getBlockState(pos);
        var targetBlock = targetBlockState.getBlock();
        if (targetBlock.equals(Blocks.BARRIER)) {
            return;
        }
        if (targetBlockState.isIn(BlockTags.REPLACEABLE) || targetBlock == LavaSurvival.BURNING_BLOCK) {
            world.setBlockState(pos, LavaSurvival.INFINITE_LAVA.getDefaultState());
        }
        if (targetBlockState.isBurnable()) {
            if (LavaSurvivalUtil.randomChance(0.15f)) {
                world.setBlockState(pos, LavaSurvival.BURNING_BLOCK.getDefaultState());
                world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.2f, LavaSurvivalUtil.randomFloat(0.7f, 0.9f));
            }
        }
        if (targetBlockState.isIn(BlockTags.ENDERMAN_HOLDABLE) || targetBlockState.isIn(BlockTags.OVERWORLD_CARVER_REPLACEABLES) || targetBlockState.isIn(BlockTags.NETHER_CARVER_REPLACEABLES)) {
            if (LavaSurvivalUtil.randomChance(0.08f)) {
                world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.2f, LavaSurvivalUtil.randomFloat(0.7f, 0.9f));

                world.setBlockState(pos, LavaSurvival.BURNING_BLOCK.getDefaultState());
            }
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, InfiniteLavaBlockEntity be) {
        if (world instanceof ServerWorld serverWorld) {
            var gameSpaceManager = GameSpaceManager.get().byWorld(world);
            var time = gameSpaceManager.getTime();


            if (time % spreadTime == 0) {
                spreadLava(serverWorld, pos.north());
                spreadLava(serverWorld, pos.east());
                spreadLava(serverWorld, pos.south());
                spreadLava(serverWorld, pos.west());
                spreadLava(serverWorld, pos.up());
                spreadLava(serverWorld, pos.down());

                world.setBlockState(pos, LavaSurvival.INFINITE_LAVA_STILL.getDefaultState());
            }
        }
    }
}
