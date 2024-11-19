package poeticrainbow.lavasurvival.block;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import poeticrainbow.lavasurvival.LavaSurvival;
import xyz.nucleoid.packettweaker.PacketContext;

public class InfiniteLavaBlock extends BlockWithEntity implements PolymerBlock  {
    public InfiniteLavaBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new InfiniteLavaBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return InfiniteLavaBlock.validateTicker(type, LavaSurvival.INFINITE_LAVA_BLOCK_ENTITY, (InfiniteLavaBlockEntity::tick));
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (entity instanceof ServerPlayerEntity player) {
            player.damage((ServerWorld) world, world.getDamageSources().lava(), 4.0f);
        }
        if (entity instanceof ItemEntity item) {
            item.kill((ServerWorld) world);
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.LAVA.getDefaultState();
    }
}
