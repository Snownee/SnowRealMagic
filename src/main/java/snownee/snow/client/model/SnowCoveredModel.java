package snownee.snow.client.model;

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.entity.RenderData;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.client.ClientHooks;
import snownee.snow.client.FabricRendererRenderAPI;

public class SnowCoveredModel extends WrapperBlockStateModel {

	public SnowCoveredModel(BlockStateModel model) {
		wrapped = model;
	}

	@Override
	public void emitQuads(
			QuadEmitter emitter,
			BlockAndTintGetter level,
			BlockPos pos,
			BlockState state,
			RandomSource random,
			Predicate<@Nullable Direction> cullTest) {
		Object data = ((FabricBlockGetter) level).getBlockEntityRenderData(pos);
		if (!(data instanceof RenderData(BlockState camo, SnowBlockEntity.Options options))) {
			return;
		}
		FabricRendererRenderAPI api = new FabricRendererRenderAPI(
				new SRMQuadEmitter(emitter),
				level,
				pos,
				state,
				random,
				cullTest,
				wrapped);
		ClientHooks.renderHook(state, camo, options, null, api);
	}
}
