package snownee.snow.compat.farmersdelight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.KiwiModule.NoItem;
import snownee.kiwi.KiwiModule.RenderLayer;
import snownee.kiwi.RenderLayerEnum;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.convert.CoveredBlockConverter;
import vectorwing.farmersdelight.common.block.RopeFenceBlock;

@NotNullByDefault
@KiwiModule(value = FarmersDelightCompat.ID, dependencies = FarmersDelightCompat.ID)
public class FarmersDelightCompat extends AbstractModule {
	public static final String ID = "farmersdelight";

	@NoItem
	@Name("snow_rope_fence")
	@RenderLayer(RenderLayerEnum.CUTOUT)
	public static final KiwiGO<SnowRopeFenceBlock> ROPE_FENCE = go(() -> new SnowRopeFenceBlock(blockProp(Blocks.OAK_FENCE)
			.mapColor(MapColor.SNOW)
			.sound(SoundType.SNOW)
			.randomTicks()));

	@Override
	protected void postInit(PostInitEvent event) {
		event.enqueueWork(() -> CoreModule.CONVERTERS.converters.putBefore(
				SnowRealMagic.id("fence"),
				ResourceLocation.fromNamespaceAndPath(ID, "fence"),
				new CoveredBlockConverter(RopeFenceBlock.class, ROPE_FENCE.get().defaultBlockState())));
	}
}