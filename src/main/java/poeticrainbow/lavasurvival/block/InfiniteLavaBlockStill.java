package poeticrainbow.lavasurvival.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;
import poeticrainbow.lavasurvival.LavaSurvival;
import xyz.nucleoid.packettweaker.PacketContext;

public class InfiniteLavaBlockStill extends Block implements PolymerBlock {
    public InfiniteLavaBlockStill(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.LAVA.getDefaultState();
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (entity instanceof ServerPlayerEntity player) {
            player.damage((ServerWorld) world, world.getDamageSources().lava(), 4.0f);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, null, notify);
        if (sourceBlock != LavaSurvival.INFINITE_LAVA && sourceBlock != LavaSurvival.INFINITE_LAVA_STILL) {
            world.setBlockState(pos, LavaSurvival.INFINITE_LAVA.getDefaultState());
        }
    }
}
