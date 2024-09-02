package snownee.snow.client.model;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.client.FabricRendererRenderAPI;
import snownee.snow.client.SnowClient;

public class SnowCoveredModel extends ForwardingBakedModel {

	public SnowCoveredModel(BakedModel model) {
		wrapped = model;
	}

	@Override
	public void emitBlockQuads(
			BlockAndTintGetter blockView,
			BlockState state,
			BlockPos pos,
			Supplier<RandomSource> randomSupplier,
			RenderContext context) {
		ModelData data = context.getModelData();
		if (data == null) return;
		FabricRendererRenderAPI api = new FabricRendererRenderAPI(context, state, wrapped);
		SnowClient.renderHook(blockView, pos, state, data.has(SnowBlockEntity.BLOCKSTATE) ? data.get(SnowBlockEntity.BLOCKSTATE) :
				Blocks.AIR.defaultBlockState(), data.has(SnowBlockEntity.OPTIONS) ? data.get(SnowBlockEntity.OPTIONS) : SnowClient.fallbackOptions, context.getRenderLayer(), randomSupplier, true, api);
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
