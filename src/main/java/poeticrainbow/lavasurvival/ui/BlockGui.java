package poeticrainbow.lavasurvival.ui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import poeticrainbow.lavasurvival.util.LavaSurvivalUtil;

import java.util.ArrayList;

public class BlockGui extends SimpleGui {
    private final ArrayList<Item> items;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     *                              will be treated as slots of this gui
     */
    public BlockGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        items = new ArrayList<Item>();
        setTitle(Text.translatable("gui.blockmenu.title"));
        drawUi();
    }

    private void drawUi() {
        // Scuffed but works
        items.add(Items.STONE);
        items.add(Items.COBBLESTONE);
        items.add(Items.BRICKS);
        items.add(Items.DIRT);
        items.add(Items.OAK_PLANKS);
        items.add(Items.OAK_LOG);
        items.add(Items.OAK_LEAVES);
        items.add(Items.GLASS);
        items.add(Items.SMOOTH_STONE_SLAB);
        items.add(Items.MOSSY_COBBLESTONE);
        items.add(Items.OAK_SAPLING);
        items.add(Items.DANDELION);
        items.add(Items.POPPY);
        items.add(Items.BROWN_MUSHROOM);
        items.add(Items.RED_MUSHROOM);
        items.add(Items.SAND);
        items.add(Items.GRAVEL);
        items.add(Items.SPONGE);
        items.add(Items.RED_WOOL);
        items.add(Items.ORANGE_WOOL);
        items.add(Items.YELLOW_WOOL);
        items.add(Items.LIME_WOOL);
        items.add(Items.GREEN_WOOL);
        items.add(Items.LIGHT_BLUE_WOOL);
        items.add(Items.CYAN_WOOL);
        items.add(Items.BLUE_WOOL);
        items.add(Items.PURPLE_WOOL);
        items.add(Items.MAGENTA_WOOL);
        items.add(Items.PINK_WOOL);
        items.add(Items.BROWN_WOOL);
        items.add(Items.BLACK_WOOL);
        items.add(Items.GRAY_WOOL);
        items.add(Items.LIGHT_GRAY_WOOL);
        items.add(Items.WHITE_WOOL);
        items.add(Items.COAL_ORE);
        items.add(Items.IRON_ORE);
        items.add(Items.GOLD_ORE);
        items.add(Items.IRON_BLOCK);
        items.add(Items.GOLD_BLOCK);
        items.add(Items.BOOKSHELF);
        items.add(Items.TNT);
        items.add(Items.OBSIDIAN);

        for (Item item : items) {
            addSlot(item.getDefaultStack());
        }
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        var itemstack = element.getItemStack();
        itemstack.setCount(64);
        var player = getPlayer();
        player.giveItemStack(itemstack);

        player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.5f, LavaSurvivalUtil.randomFloat(0.7f, 1.0f));

        // Reset the item
        setSlot(index, items.get(index).getDefaultStack());
        return super.onClick(index, type, action, element);
    }
}
