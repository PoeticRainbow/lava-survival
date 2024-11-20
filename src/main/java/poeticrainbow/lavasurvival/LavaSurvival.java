package poeticrainbow.lavasurvival;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
import xyz.nucleoid.plasmid.api.game.GameType;

public class LavaSurvival implements ModInitializer {
	public static final String MOD_ID = "lavasurvival";
    public static final Logger LOGGER = LoggerFactory.getLogger("lavasurvival");

	public static final RegistryKey<Block> INFINITE_LAVA_KEY = RegistryKey.of(RegistryKeys.BLOCK, id("infinite_lava"));
	public static final Block INFINITE_LAVA = register(
			new InfiniteLavaBlock(AbstractBlock.Settings.create().nonOpaque().solid().luminance((state) -> 15).registryKey(INFINITE_LAVA_KEY)),
			INFINITE_LAVA_KEY);

	public static final RegistryKey<Block> INFINITE_LAVA_STILL_KEY = RegistryKey.of(RegistryKeys.BLOCK, id("infinite_lava_still"));
	public static final Block INFINITE_LAVA_STILL = register(new InfiniteLavaBlockStill(
			AbstractBlock.Settings.create().nonOpaque().solid().luminance((state) -> 15).registryKey(INFINITE_LAVA_STILL_KEY)),
			INFINITE_LAVA_STILL_KEY);

	public static final RegistryKey<Block> BURNING_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, id("burning_block"));
	public static final Block BURNING_BLOCK = register(
			new BurningBlock(AbstractBlock.Settings.create().solid().ticksRandomly().luminance((state) -> 15).registryKey(BURNING_BLOCK_KEY)),
			BURNING_BLOCK_KEY);

	public static final RegistryKey<Item> BLOCK_MENU_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, id("block_menu"));
	public static final Item BLOCK_MENU_ITEM = register(
			new BlockMenuItem(new Item.Settings().registryKey(BLOCK_MENU_ITEM_KEY)),
			BLOCK_MENU_ITEM_KEY);

	public static final BlockEntityType<InfiniteLavaBlockEntity> INFINITE_LAVA_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			id("infinite_lava_entity"),
			FabricBlockEntityTypeBuilder.create(InfiniteLavaBlockEntity::new, LavaSurvival.INFINITE_LAVA).build(null)
	);

	public static final GameType<LavaSurvivalConfig> LAVASURVIVAL = GameType.register(
			id("lavasurvival"),
			LavaSurvivalConfig.CODEC,
			LavaSurvivalWaitingPhase::open);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		RegistrySyncUtils.setServerEntry(Registries.BLOCK_ENTITY_TYPE, INFINITE_LAVA_BLOCK_ENTITY);

		PolymerBlockUtils.registerBlockEntity(INFINITE_LAVA_BLOCK_ENTITY);
	}

	public static Block register(Block block, RegistryKey<Block> key) {
		Registry.register(Registries.BLOCK, key, block);
		return block;
	}

	public static Item register(Item item, RegistryKey<Item> key) {
		Registry.register(Registries.ITEM, key, item);
		return item;
	}
}