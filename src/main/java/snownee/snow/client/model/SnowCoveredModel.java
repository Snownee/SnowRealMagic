package snownee.snow.client.model;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.client.FabricRendererRenderAPI;
import snownee.snow.client.SnowClient;

public class SnowCoveredModel extends ForwardingBakedModel {

	public SnowCoveredModel(BakedModel model) {
		wrapped = model;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		Object data = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		if (!(data instanceof SnowBlockEntity))
			return;
		SnowBlockEntity be = (SnowBlockEntity) data;
		SnowClient.renderHook(blockView, pos, state, be.getState(), be.options, null, randomSupplier, true, new FabricRendererRenderAPI(context));
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
