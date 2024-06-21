package poeticrainbow.lavasurvival.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import poeticrainbow.lavasurvival.LavaSurvival;

public class LavaSurvivalUtil {
    static Random random = Random.create();

    public static Vec3d findSafeSpot(BlockPos startPos, ServerWorld world) {
        var blockState = world.getBlockState(startPos);
        if (!blockState.isOpaque() && blockState != LavaSurvival.INFINITE_LAVA.getDefaultState()) {
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
        return createItemWithNbt(item, 1, "{Unbreakable:1b}");
    }

    public static ItemStack createItemWithNbt(Item item, int count, String nbtString) {
        var nbt = new NbtCompound();

        try {
            nbt = StringNbtReader.parse(nbtString);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        var itemstack = new ItemStack(item, count);
        itemstack.setNbt(nbt);

        return itemstack;
    }
}
