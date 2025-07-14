package poeticrainbow.lavasurvival.ui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class BlockGui extends SimpleGui {
    private static final Text TITLE = Text.translatable("gui.blockmenu.title");
    private static final LoreComponent BUTTON_LORE = new LoreComponent(
        List.of(Text.translatable("item.lavasurvival.block_menu.description")
                    .setStyle(Style.EMPTY.withItalic(false))
                    .formatted(Formatting.GREEN))
    );
    private static final List<Item> items = List.of(
        Items.STONE, Items.COBBLESTONE, Items.BRICKS, Items.DIRT, Items.OAK_PLANKS,
        Items.OAK_LOG, Items.OAK_LEAVES, Items.GLASS, Items.SMOOTH_STONE_SLAB,
        Items.MOSSY_COBBLESTONE, Items.OAK_SAPLING, Items.DANDELION, Items.POPPY,
        Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.SAND, Items.GRAVEL, Items.SPONGE,
        Items.RED_WOOL, Items.ORANGE_WOOL, Items.YELLOW_WOOL, Items.LIME_WOOL, Items.GREEN_WOOL,
        Items.LIGHT_BLUE_WOOL, Items.CYAN_WOOL, Items.BLUE_WOOL, Items.PURPLE_WOOL, Items.MAGENTA_WOOL,
        Items.PINK_WOOL, Items.BROWN_WOOL, Items.BLACK_WOOL, Items.GRAY_WOOL, Items.LIGHT_GRAY_WOOL,
        Items.WHITE_WOOL, Items.COAL_ORE, Items.IRON_ORE, Items.GOLD_ORE, Items.IRON_BLOCK,
        Items.GOLD_BLOCK, Items.BOOKSHELF, Items.TNT, Items.OBSIDIAN
    );

    public BlockGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        setTitle(TITLE);
        setLockPlayerInventory(true);

        for (int i = 0; i < items.size(); i++) {
            if (i >= getSize()) break;
            setSlot(i, buttonStack(items.get(i)));
        }
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        if (element == null) {
            return false;
        }
        var itemstack = element.getItemStack().copy();
        itemstack.remove(DataComponentTypes.LORE);
        itemstack.setCount(64);

        var player = getPlayer();
        player.playSoundToPlayer(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.UI, 1f, 1f);
        player.sendMessage(Text.literal("+ 64x ")
                               .append(Text.translatable(itemstack.getItem().getTranslationKey()))
                               .formatted(Formatting.GREEN));
        player.giveItemStack(itemstack);
        return true;
    }

    public ItemStack buttonStack(Item item) {
        var stack = item.getDefaultStack();
        stack.set(DataComponentTypes.LORE, BUTTON_LORE);
        return stack;
    }
}
