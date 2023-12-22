package snownee.snow.client.model;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.entity.RenderData;
import snownee.snow.client.FabricRendererRenderAPI;
import snownee.snow.client.SnowClient;

public class SnowCoveredModel extends ForwardingBakedModel {

	public SnowCoveredModel(BakedModel model) {
		wrapped = model;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		Object data = blockView.getBlockEntityRenderData(pos);
		if (!(data instanceof RenderData renderData))
			return;
		SnowClient.renderHook(blockView, pos, state, renderData.state(), renderData.options(), null, randomSupplier, true, new FabricRendererRenderAPI(context));
	}

	@Override
	public ItemTransforms getTransforms() {
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

}
