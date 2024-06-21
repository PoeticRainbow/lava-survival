package poeticrainbow.lavasurvival.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import poeticrainbow.lavasurvival.LavaSurvival;

public class BurningBlock extends Block implements PolymerBlock {
    public BurningBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, LavaSurvival.INFINITE_LAVA.getDefaultState());
        super.randomTick(state, world, pos, random);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.MAGMA_BLOCK;
    }
}
