package poeticrainbow.lavasurvival.ui;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;
import xyz.nucleoid.packettweaker.PacketContext;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

public class BlockMenuItem extends Item implements PolymerItem {
    public BlockMenuItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.BOOK;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return Identifier.of("minecraft:book");
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            var gamespace = GameSpaceManager.get().byWorld(world);
            if (gamespace == null) {
                user.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.UI, 0.7f, LavaSurvivalUtil.randomFloat(1.0f, 1.4f));
                return ActionResult.SUCCESS;
            }

            var blockMenu = new BlockGui(player);
            blockMenu.open();
            user.playSoundToPlayer(SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.UI, 0.7f, 1f);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }
}
