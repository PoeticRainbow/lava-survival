package poeticrainbow.lavasurvival.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import poeticrainbow.lavasurvival.ui.BlockGui;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

public class BlockMenuItem extends Item implements PolymerItem {
    public BlockMenuItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.BOOK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            var gamespace = GameSpaceManager.get().byWorld(world);
            if (gamespace == null) {
                user.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.7f, LavaSurvivalUtil.randomFloat(1.0f, 1.4f));
                return TypedActionResult.success(user.getMainHandStack());
            }

            var blockMenu = new BlockGui(player);
            blockMenu.open();
            user.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 0.7f, LavaSurvivalUtil.randomFloat(1.0f, 1.4f));
            return TypedActionResult.success(user.getMainHandStack(), true);
        }
        return TypedActionResult.fail(user.getMainHandStack());
    }
}
