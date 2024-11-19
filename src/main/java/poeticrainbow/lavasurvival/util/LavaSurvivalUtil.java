package poeticrainbow.lavasurvival.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import poeticrainbow.lavasurvival.LavaSurvival;

import java.util.Optional;

public class LavaSurvivalUtil {
    static Random random = Random.create();

    public static Vec3d findSafeSpot(BlockPos startPos, ServerWorld world) {
        var blockState = world.getBlockState(startPos);
        if (!blockState.isOpaque() && blockState.getBlock() != LavaSurvival.INFINITE_LAVA) {
            return startPos.toCenterPos();
        }
        return findSafeSpot(startPos.up(), world);
    }

    public static BlockPos getTopBlock(BlockPos startPos, ServerWorld world) {
        for (var i = 0; i + startPos.getY() < 320 ; i++) {
            var currentBlock = world.getBlockState(startPos.up(i));
            if (currentBlock.isAir()) {
                return startPos.up(i);
            }
        }
        return startPos;
    }

    public static BlockPos getRandomBlockPos(int xbound, int zbound) {
        var randomX = random.nextFloat();
        var randomZ = random.nextFloat();

        var posX = (int)Math.floor(randomX * xbound * 16);
        var posZ = (int)Math.floor(randomZ * zbound * 16);

        return new BlockPos(posX, 64, posZ);
    }

    public static float randomFloat(float min, float max) {
        var randomFloat = random.nextFloat();
        return randomFloat * (max - min) + min;
    }

    public static boolean randomChance(float chance) {
        var randomValue = random.nextFloat();
        return randomValue <= chance;
    }

    public static ItemStack createUnbreakableTool(Item item) {
        var itemStack = item.getDefaultStack();
        itemStack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
        return itemStack;
    }

//    public static ItemStack createItemWithNbt(Item item, int count, String nbtString) {
//        var nbt = new NbtCompound();
//
//        try {
//            nbt = StringNbtReader.parse(nbtString);
//        } catch (CommandSyntaxException e) {
//            throw new RuntimeException(e);
//        }
//
//        var itemstack = new ItemStack(item, count);
//        itemstack.setNbt(nbt);
//
//        return itemstack;
//    }

    public static ResourcePackSendS2CPacket createResourcePackPacket(PlayerEntity player, String url, String hash, boolean required, Text prompt) {
        ResourcePackSendS2CPacket packet = new ResourcePackSendS2CPacket(player.getUuid(), url, hash, required, Optional.of(prompt));
        return packet;
    }
}
