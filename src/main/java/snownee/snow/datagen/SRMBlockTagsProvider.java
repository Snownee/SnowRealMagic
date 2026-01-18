package snownee.snow.datagen;

import static snownee.snow.CoreModule.FENCE;
import static snownee.snow.CoreModule.FENCE2;
import static snownee.snow.CoreModule.FENCE_GATE;
import static snownee.snow.CoreModule.SLAB;
import static snownee.snow.CoreModule.SNOWY_DOUBLE_PLANT_LOWER;
import static snownee.snow.CoreModule.SNOWY_DOUBLE_PLANT_UPPER;
import static snownee.snow.CoreModule.SNOWY_PLANT;
import static snownee.snow.CoreModule.SNOWY_SETTING;
import static snownee.snow.CoreModule.SNOW_BLOCK;
import static snownee.snow.CoreModule.SNOW_EXTRA_COLLISION_BLOCK;
import static snownee.snow.CoreModule.STAIRS;
import static snownee.snow.CoreModule.WALL;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.snow.CoreModule;
import snownee.snow.block.ExtraCollisionSnowLayerBlock;

public class SRMBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {
	TagKey<Block> GRASS = AbstractModule.blockTag("c", "grass");
	TagKey<Block> MUSHROOMS = AbstractModule.blockTag("c", "mushrooms");

	public SRMBlockTagsProvider(
			FabricPackOutput output,
			CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider wrapperLookup) {
		for (Block block : Stream.of(
				SNOW_EXTRA_COLLISION_BLOCK,
				SNOWY_DOUBLE_PLANT_LOWER,
				SNOWY_DOUBLE_PLANT_UPPER,
				SNOW_BLOCK,
				SNOWY_PLANT).map(KiwiGO::get).toList()) {
			valueLookupBuilder(CoreModule.SNOW_TAG).add(block);
			if (!(block instanceof ExtraCollisionSnowLayerBlock)) {
				valueLookupBuilder(BlockTags.GOATS_SPAWNABLE_ON).add(block);
				valueLookupBuilder(BlockTags.MOOSHROOMS_SPAWNABLE_ON).add(block);
				valueLookupBuilder(BlockTags.RABBITS_SPAWNABLE_ON).add(block);
				valueLookupBuilder(BlockTags.FOXES_SPAWNABLE_ON).add(block);
				valueLookupBuilder(BlockTags.WOLVES_SPAWNABLE_ON).add(block);
			}
		}

		valueLookupBuilder(BlockTags.SNOW).addTag(CoreModule.SNOW_TAG);
		valueLookupBuilder(BlockTags.MINEABLE_WITH_SHOVEL).addTag(CoreModule.SNOW_TAG);
		valueLookupBuilder(BlockTags.INSIDE_STEP_SOUND_BLOCKS).addTag(CoreModule.SNOW_TAG);

		valueLookupBuilder(BlockTags.STAIRS).add(STAIRS.get());
		valueLookupBuilder(BlockTags.SLABS).add(SLAB.get());
		valueLookupBuilder(BlockTags.FENCE_GATES).add(FENCE_GATE.get());
		valueLookupBuilder(BlockTags.FENCES).add(FENCE2.get());
		valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE).add(FENCE2.get());
		valueLookupBuilder(BlockTags.WOODEN_FENCES).add(FENCE.get());
		valueLookupBuilder(BlockTags.WALLS).add(WALL.get());

		valueLookupBuilder(BlockTags.SUPPORT_OVERRIDE_SNOW_LAYER).forceAddTag(BlockTags.LEAVES);
		valueLookupBuilder(SNOWY_SETTING).forceAddTag(BlockTags.SNOW);
		valueLookupBuilder(SNOWY_SETTING).add(FENCE.get(), FENCE2.get(), FENCE_GATE.get(), WALL.get());

		valueLookupBuilder(CoreModule.CANNOT_ACCUMULATE_ON).add(Blocks.HAY_BLOCK).forceAddTag(BlockTags.SLABS);

		valueLookupBuilder(GRASS).add(Blocks.SHORT_GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN);
		valueLookupBuilder(MUSHROOMS).add(Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM);
	}
}
