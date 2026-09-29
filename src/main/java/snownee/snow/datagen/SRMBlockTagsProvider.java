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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.datagen.KiwiBlockTagsProvider;
import snownee.snow.CoreModule;
import snownee.snow.block.ExtraCollisionSnowLayerBlock;

public class SRMBlockTagsProvider extends KiwiBlockTagsProvider {
	TagKey<Block> GRASS = AbstractModule.blockTag("c", "grass");
	TagKey<Block> MUSHROOMS = AbstractModule.blockTag("c", "mushrooms");

	public SRMBlockTagsProvider(
			FabricPackOutput output,
			CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
		super(output, registryLookupFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		for (Block block : Stream.of(
				SNOW_EXTRA_COLLISION_BLOCK,
				SNOWY_DOUBLE_PLANT_LOWER,
				SNOWY_DOUBLE_PLANT_UPPER,
				SNOW_BLOCK,
				SNOWY_PLANT).map(KiwiGO::get).toList()) {
			builder(CoreModule.SNOW_TAG).add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
			if (!(block instanceof ExtraCollisionSnowLayerBlock)) {
				builder(BlockTags.GOATS_SPAWNABLE_ON).add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
				builder(BlockTags.MOOSHROOMS_SPAWNABLE_ON).add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
				builder(BlockTags.RABBITS_SPAWNABLE_ON).add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
				builder(BlockTags.FOXES_SPAWNABLE_ON).add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
				builder(BlockTags.WOLVES_SPAWNABLE_ON).add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
			}
		}

		builder(BlockTags.SNOW).addTag(CoreModule.SNOW_TAG);
		builder(BlockTags.MINEABLE_WITH_SHOVEL).addTag(CoreModule.SNOW_TAG);
		builder(BlockTags.INSIDE_STEP_SOUND_BLOCKS).addTag(CoreModule.SNOW_TAG);

		builder(BlockTags.STAIRS).add(BuiltInRegistries.BLOCK.getResourceKey(STAIRS.get()).orElseThrow());
		builder(BlockTags.SLABS).add(BuiltInRegistries.BLOCK.getResourceKey(SLAB.get()).orElseThrow());
		builder(BlockTags.FENCE_GATES).add(BuiltInRegistries.BLOCK.getResourceKey(FENCE_GATE.get()).orElseThrow());
		builder(BlockTags.FENCES).add(BuiltInRegistries.BLOCK.getResourceKey(FENCE2.get()).orElseThrow());
		builder(BlockTags.MINEABLE_WITH_PICKAXE).add(BuiltInRegistries.BLOCK.getResourceKey(FENCE2.get()).orElseThrow());
		builder(BlockTags.WOODEN_FENCES).add(BuiltInRegistries.BLOCK.getResourceKey(FENCE.get()).orElseThrow());
		builder(BlockTags.WALLS).add(BuiltInRegistries.BLOCK.getResourceKey(WALL.get()).orElseThrow());

		builder(BlockTags.SUPPORT_OVERRIDE_SNOW_LAYER).forceAddTag(BlockTags.LEAVES);
		builder(SNOWY_SETTING).forceAddTag(BlockTags.SNOW);
		builder(SNOWY_SETTING).add(BuiltInRegistries.BLOCK.getResourceKey(FENCE.get()).orElseThrow(), BuiltInRegistries.BLOCK.getResourceKey(FENCE2.get()).orElseThrow(), BuiltInRegistries.BLOCK.getResourceKey(FENCE_GATE.get()).orElseThrow(), BuiltInRegistries.BLOCK.getResourceKey(WALL.get()).orElseThrow());

		builder(CoreModule.CANNOT_ACCUMULATE_ON).add(BuiltInRegistries.BLOCK.getResourceKey(Blocks.HAY_BLOCK).orElseThrow()).forceAddTag(BlockTags.SLABS);

		builder(GRASS).add(BuiltInRegistries.BLOCK.getResourceKey(Blocks.SHORT_GRASS).orElseThrow(), BuiltInRegistries.BLOCK.getResourceKey(Blocks.FERN).orElseThrow(), BuiltInRegistries.BLOCK.getResourceKey(Blocks.TALL_GRASS).orElseThrow(), BuiltInRegistries.BLOCK.getResourceKey(Blocks.LARGE_FERN).orElseThrow());
		builder(MUSHROOMS).add(BuiltInRegistries.BLOCK.getResourceKey(Blocks.RED_MUSHROOM).orElseThrow(), BuiltInRegistries.BLOCK.getResourceKey(Blocks.BROWN_MUSHROOM).orElseThrow());
	}
}
