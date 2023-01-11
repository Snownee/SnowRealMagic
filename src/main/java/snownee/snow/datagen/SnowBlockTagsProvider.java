package snownee.snow.datagen;

import static snownee.snow.CoreModule.*;

import net.minecraft.data.DataGenerator;
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

	public SnowBlockTagsProvider(DataGenerator pGenerator, ExistingFileHelper existingFileHelper) {
		super(pGenerator, SnowRealMagic.MODID, existingFileHelper);
		helper = new TagsProviderHelper<>(this);
	}

	@Override
	protected void addTags() {
		helper.getModEntries().filter($ -> !SLAB.is($) && !STAIRS.is($) && !FENCE_GATE.is($)).forEach(this::processTools);

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

		tag(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON).addTag(BlockTags.LEAVES);
		tag(BOTTOM_SNOW).addTag(BlockTags.SNOW);
		helper.add(BOTTOM_SNOW, FENCE, FENCE2, FENCE_GATE, WALL);

		tag(CoreModule.NOT_CONTAINABLES);
		tag(CoreModule.CANNOT_ACCUMULATE_ON).add(Blocks.HAY_BLOCK);
	}

}
