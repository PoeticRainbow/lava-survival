package poeticrainbow.lavasurvival;

import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.api.ModInitializer;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poeticrainbow.lavasurvival.block.BurningBlock;
import poeticrainbow.lavasurvival.block.InfiniteLavaBlock;
import poeticrainbow.lavasurvival.block.InfiniteLavaBlockEntity;
import poeticrainbow.lavasurvival.block.InfiniteLavaBlockStill;
import poeticrainbow.lavasurvival.game.LavaSurvivalConfig;
import poeticrainbow.lavasurvival.game.phases.LavaSurvivalWaitingPhase;
import poeticrainbow.lavasurvival.item.BlockMenuItem;
import xyz.nucleoid.plasmid.game.GameType;

public class LavaSurvival implements ModInitializer {
	public static final String ID = "lavasurvival";
    public static final Logger LOGGER = LoggerFactory.getLogger("lavasurvival");

	public static final Block INFINITE_LAVA = new InfiniteLavaBlock(AbstractBlock.Settings.create().nonOpaque().solid());
	public static final Block INFINITE_LAVA_STILL = new InfiniteLavaBlockStill(AbstractBlock.Settings.create().nonOpaque().solid());
	public static final Block BURNING_BLOCK = new BurningBlock(AbstractBlock.Settings.create().solid().ticksRandomly());

	public static final Item BLOCK_MENU_ITEM = new BlockMenuItem(new Item.Settings());

	public static final BlockEntityType<InfiniteLavaBlockEntity> INFINITE_LAVA_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			id("infinite_lava_entity"),
			BlockEntityType.Builder.create(InfiniteLavaBlockEntity::new, LavaSurvival.INFINITE_LAVA).build(null)
	);

	public static final GameType<LavaSurvivalConfig> LAVASURVIVAL = GameType.register(
			id("lavasurvival"),
			LavaSurvivalConfig.CODEC,
			LavaSurvivalWaitingPhase::open);

	public static Identifier id(String path) {
		return new Identifier(LavaSurvival.ID, path);
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, id("infinite_lava"), INFINITE_LAVA);
		Registry.register(Registries.BLOCK, id("infinite_lava_still"), INFINITE_LAVA_STILL);
		Registry.register(Registries.BLOCK, id("burning_block"), BURNING_BLOCK);

		Registry.register(Registries.ITEM, id("block_menu"), BLOCK_MENU_ITEM);

		RegistrySyncUtils.setServerEntry(Registries.BLOCK_ENTITY_TYPE, INFINITE_LAVA_BLOCK_ENTITY);

	}
}