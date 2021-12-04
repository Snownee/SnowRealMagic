package snownee.snow.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import snownee.kiwi.data.provider.KiwiBlockTagsProvider;
import snownee.snow.SnowRealMagic;

public class SnowBlockTagsProvider extends KiwiBlockTagsProvider {

	public SnowBlockTagsProvider(DataGenerator pGenerator, ExistingFileHelper existingFileHelper) {
		super(pGenerator, SnowRealMagic.MODID, existingFileHelper);
	}

}
