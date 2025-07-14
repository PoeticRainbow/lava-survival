package poeticrainbow.lavasurvival;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class LavaSurvivalDataGenerator implements DataGeneratorEntrypoint {
	public static class ModBlockTagGenerator extends FabricTagProvider.BlockTagProvider {
		public static final TagKey<Block> ONE_POINT_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("lavasurvival:one_point_blocks"));
		public static final TagKey<Block> THREE_POINT_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("lavasurvival:three_point_blocks"));
		public static final TagKey<Block> FIVE_POINT_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("lavasurvival:five_point_blocks"));

		public ModBlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
			super(output, completableFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
			getTagBuilder(ONE_POINT_BLOCKS)
					.add(Registries.BLOCK.getId(Blocks.GRAVEL))
					.addOptionalTag(BlockTags.DIRT.id())
					.addOptionalTag(BlockTags.SAND.id());
		}
	}

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ModBlockTagGenerator::new);
		LavaSurvival.LOGGER.info("Registering block tags for Lava Survival");
	}
}
