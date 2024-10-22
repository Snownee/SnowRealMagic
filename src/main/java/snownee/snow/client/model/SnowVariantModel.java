package snownee.snow.client.model;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.block.entity.RenderData;
import snownee.snow.client.SnowClientConfig;

public class SnowVariantModel extends ForwardingBakedModel {

	private final BakedModel variantModel;

	public SnowVariantModel(BakedModel model, BakedModel variantModel) {
		wrapped = model;
		this.variantModel = variantModel;
	}

	@Override
	public void emitBlockQuads(
			BlockAndTintGetter blockView,
			BlockState state,
			BlockPos pos,
			Supplier<RandomSource> randomSupplier,
			RenderContext context) {
		BakedModel model = null;
		if (SnowClientConfig.snowVariants && pos != null) {
			if (((FabricBlockView) blockView).getBlockEntityRenderData(pos) instanceof RenderData) {
				model = variantModel;
			} else if (state.hasProperty(DoublePlantBlock.HALF) &&
					CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(blockView.getBlockState(pos.below()))) {
				model = variantModel;
			}
		}
		if (model == null) {
			model = wrapped;
		}
		((FabricBakedModel) model).emitBlockQuads(blockView, state, pos, randomSupplier, context);
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

}
