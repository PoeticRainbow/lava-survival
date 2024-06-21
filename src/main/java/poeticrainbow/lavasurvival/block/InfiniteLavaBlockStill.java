package poeticrainbow.lavasurvival.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import poeticrainbow.lavasurvival.LavaSurvival;

public class InfiniteLavaBlockStill extends Block implements PolymerBlock {
    public InfiniteLavaBlockStill(Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.LAVA;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (entity instanceof ServerPlayerEntity player) {
            player.damage(world.getDamageSources().lava(), 4.0f);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (sourceBlock != LavaSurvival.INFINITE_LAVA && sourceBlock != LavaSurvival.INFINITE_LAVA_STILL) {
            world.setBlockState(pos, LavaSurvival.INFINITE_LAVA.getDefaultState());
        }
    }
}
