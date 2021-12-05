package snownee.snow.datagen;

import static snownee.kiwi.data.provider.TagsProviderHelper.getModEntries;
import static snownee.snow.CoreModule.*;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import snownee.kiwi.data.provider.KiwiBlockTagsProvider;
import snownee.snow.SnowRealMagic;

public class SnowBlockTagsProvider extends KiwiBlockTagsProvider {

	public SnowBlockTagsProvider(DataGenerator pGenerator, ExistingFileHelper existingFileHelper) {
		super(pGenerator, SnowRealMagic.MODID, existingFileHelper);
	}

	@Override
	protected void addTags() {
		getModEntries(modId, registry).filter($ -> $ != SLAB && $ != STAIRS && $ != FENCE_GATE).forEach(this::processTools);

		tag(BlockTags.MINEABLE_WITH_SHOVEL).add(TILE_BLOCK);
		tag(BlockTags.INSIDE_STEP_SOUND_BLOCKS).add(TILE_BLOCK);
		tag(BlockTags.SNOW).add(TILE_BLOCK);
		tag(BlockTags.GOATS_SPAWNABLE_ON).add(TILE_BLOCK);
		tag(BlockTags.MOOSHROOMS_SPAWNABLE_ON).add(TILE_BLOCK);
		tag(BlockTags.RABBITS_SPAWNABLE_ON).add(TILE_BLOCK);
		tag(BlockTags.FOXES_SPAWNABLE_ON).add(TILE_BLOCK);
		tag(BlockTags.WOLVES_SPAWNABLE_ON).add(TILE_BLOCK);

		tag(BlockTags.STAIRS).add(STAIRS);
		tag(BlockTags.SLABS).add(SLAB);
		tag(BlockTags.FENCE_GATES).add(FENCE_GATE);
		tag(BlockTags.FENCES).add(FENCE2);

		tag(BOTTOM_SNOW).addTag(BlockTags.SNOW).add(FENCE, FENCE2, FENCE_GATE, WALL);
	}

}
