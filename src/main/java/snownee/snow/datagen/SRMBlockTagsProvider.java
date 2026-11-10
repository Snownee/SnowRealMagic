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
import net.minecraft.resources.ResourceKey;
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

	private static ResourceKey<Block> key(Block block) {
		return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		for (Block block : Stream.of(
				SNOW_EXTRA_COLLISION_BLOCK,
				SNOWY_DOUBLE_PLANT_LOWER,
				SNOWY_DOUBLE_PLANT_UPPER,
				SNOW_BLOCK,
				SNOWY_PLANT).map(KiwiGO::get).toList()) {
			tag(CoreModule.SNOW_TAG).add(key(block));
			if (!(block instanceof ExtraCollisionSnowLayerBlock)) {
				tag(BlockTags.GOATS_SPAWNABLE_ON).add(key(block));
				tag(BlockTags.MOOSHROOMS_SPAWNABLE_ON).add(key(block));
				tag(BlockTags.RABBITS_SPAWNABLE_ON).add(key(block));
				tag(BlockTags.FOXES_SPAWNABLE_ON).add(key(block));
				tag(BlockTags.WOLVES_SPAWNABLE_ON).add(key(block));
			}
		}

		tag(BlockTags.SNOW).addTag(CoreModule.SNOW_TAG);
		tag(BlockTags.MINEABLE_WITH_SHOVEL).addTag(CoreModule.SNOW_TAG);
		tag(BlockTags.INSIDE_STEP_SOUND_BLOCKS).addTag(CoreModule.SNOW_TAG);

		tag(BlockTags.STAIRS).add(key(STAIRS.get()));
		tag(BlockTags.SLABS).add(key(SLAB.get()));
		tag(BlockTags.FENCE_GATES).add(key(FENCE_GATE.get()));
		tag(BlockTags.FENCES).add(key(FENCE2.get()));
		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(key(FENCE2.get()));
		tag(BlockTags.WOODEN_FENCES).add(key(FENCE.get()));
		tag(BlockTags.WALLS).add(key(WALL.get()));

		tag(BlockTags.SUPPORT_OVERRIDE_SNOW_LAYER).addTag(BlockTags.LEAVES);
		tag(SNOWY_SETTING).addTag(BlockTags.SNOW);
		tag(SNOWY_SETTING).add(key(FENCE.get())).add(key(FENCE2.get())).add(key(FENCE_GATE.get())).add(key(WALL.get()));

		tag(CoreModule.CANNOT_ACCUMULATE_ON).add(key(Blocks.HAY_BLOCK)).addTag(BlockTags.SLABS);

		tag(GRASS).add(key(Blocks.SHORT_GRASS)).add(key(Blocks.FERN)).add(key(Blocks.TALL_GRASS)).add(key(Blocks.LARGE_FERN));
		tag(MUSHROOMS).add(key(Blocks.RED_MUSHROOM)).add(key(Blocks.BROWN_MUSHROOM));
	}
}