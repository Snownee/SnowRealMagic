package snownee.snow.compat.sodium;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import snownee.snow.client.RenderAPI;
import snownee.snow.mixin.sodium.BlockRendererAccess;

public class RubidiumRenderAPI implements RenderAPI {

	private final BlockRendererAccess blockRenderer;
	private final ModelData modelData;
	private final long seed;
	private final LightPipelineProvider lighters;
	private final BlockOcclusionCache occlusionCache;
	private final BlockPos origin;
	private final ChunkModelBuilder buffers;

	public RubidiumRenderAPI(BlockRendererAccess blockRenderer, ModelData modelData, long seed, LightPipelineProvider lighters, BlockOcclusionCache occlusionCache, BlockPos origin, ChunkModelBuilder buffers) {
		this.blockRenderer = blockRenderer;
		this.modelData = modelData;
		this.seed = seed;
		this.lighters = lighters;
		this.occlusionCache = occlusionCache;
		this.origin = origin;
		this.buffers = buffers;
	}

	@Override
	public boolean translateYAndRender(BlockAndTintGetter world, BlockState state, BlockPos pos, @Nullable RenderType layer, Supplier<RandomSource> randomSupplier, boolean cullSides, BakedModel model, double yOffset) {
		RandomSource random = randomSupplier.get();
		if (layer != null && !model.getRenderTypes(state, random, modelData).contains(layer)) {
			return false;
		}
		Vec3 offset = state.getOffset(world, pos);
		if (yOffset != 0) {
			offset = offset.add(0, yOffset, 0);
			cullSides = false;
		}
		LightPipeline lighter = lighters.getLighter(blockRenderer.callGetLightingMode(state, model, world, pos));
		boolean rendered = false;
		for (Direction dir : DirectionUtil.ALL_DIRECTIONS) {
			random.setSeed(seed);
			List<BakedQuad> sided = model.getQuads(state, dir, random, modelData, layer);
			if (sided.isEmpty()) {
				continue;
			}
			if (!cullSides || occlusionCache.shouldDrawSide(state, world, pos, dir)) {
				blockRenderer.callRenderQuadList(world, state, pos, origin, lighter, offset, buffers, sided, dir);
				rendered = true;
			}
		}

		random.setSeed(seed);
		List<BakedQuad> all = model.getQuads(state, null, random, modelData, layer);
		if (!all.isEmpty()) {
			blockRenderer.callRenderQuadList(world, state, pos, origin, lighter, offset, buffers, all, null);
			rendered = true;
		}

		return rendered;
	}

}
