package snownee.snow.datagen;

import static snownee.snow.CoreModule.FENCE;
import static snownee.snow.CoreModule.FENCE2;
import static snownee.snow.CoreModule.FENCE_GATE;
import static snownee.snow.CoreModule.SLAB;
import static snownee.snow.CoreModule.SNOWY_SETTING;
import static snownee.snow.CoreModule.STAIRS;
import static snownee.snow.CoreModule.TILE_BLOCK;
import static snownee.snow.CoreModule.WALL;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import snownee.kiwi.datagen.provider.KiwiBlockTagsProvider;
import snownee.kiwi.datagen.provider.TagsProviderHelper;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;

public class SnowBlockTagsProvider extends KiwiBlockTagsProvider {

	private final TagsProviderHelper<Block> helper;

	public SnowBlockTagsProvider(
			PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> lookupProvider,
			ExistingFileHelper existingFileHelper) {
		super(packOutput, lookupProvider, SnowRealMagic.MODID, existingFileHelper);
		helper = new TagsProviderHelper<>(this);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		helper.add(BlockTags.MINEABLE_WITH_SHOVEL, TILE_BLOCK);
		helper.add(BlockTags.INSIDE_STEP_SOUND_BLOCKS, TILE_BLOCK);
		helper.add(BlockTags.SNOW, TILE_BLOCK);
		helper.add(BlockTags.GOATS_SPAWNABLE_ON, TILE_BLOCK);
		helper.add(BlockTags.MOOSHROOMS_SPAWNABLE_ON, TILE_BLOCK);
		helper.add(BlockTags.RABBITS_SPAWNABLE_ON, TILE_BLOCK);
		helper.add(BlockTags.FOXES_SPAWNABLE_ON, TILE_BLOCK);
		helper.add(BlockTags.WOLVES_SPAWNABLE_ON, TILE_BLOCK);

		helper.add(BlockTags.STAIRS, STAIRS);
		helper.add(BlockTags.SLABS, SLAB);
		helper.add(BlockTags.FENCE_GATES, FENCE_GATE);
		helper.add(BlockTags.FENCES, FENCE2);
		helper.add(BlockTags.MINEABLE_WITH_PICKAXE, FENCE2);
		helper.add(BlockTags.WOODEN_FENCES, FENCE);
		helper.add(BlockTags.WALLS, WALL);

		tag(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON).addTag(BlockTags.LEAVES);
		tag(SNOWY_SETTING).addTag(BlockTags.SNOW);
		helper.add(SNOWY_SETTING, FENCE, FENCE2, FENCE_GATE, WALL);

		tag(CoreModule.CANNOT_ACCUMULATE_ON).add(Blocks.HAY_BLOCK).addTag(BlockTags.SLABS);
	}

}
