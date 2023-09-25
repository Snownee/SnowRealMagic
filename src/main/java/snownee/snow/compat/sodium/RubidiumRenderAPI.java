package snownee.snow.compat.sodium;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.util.DirectionUtil;
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
	private final BlockRenderContext ctx;
	private final ChunkBuildBuffers buffers;

	public RubidiumRenderAPI(BlockRendererAccess blockRenderer, BlockRenderContext ctx, ChunkBuildBuffers buffers) {
		this.blockRenderer = blockRenderer;
		this.ctx = ctx;
		this.buffers = buffers;
	}

	@Override
	public boolean translateYAndRender(BlockAndTintGetter world, BlockState state, BlockPos pos, @Nullable RenderType layer, Supplier<RandomSource> randomSupplier, boolean cullSides, BakedModel model, double yOffset) {
		RandomSource random = randomSupplier.get();
		ModelData modelData = ctx.modelData();
		if (layer != null && !model.getRenderTypes(state, random, modelData).contains(layer)) {
			return false;
		}
		Material material = DefaultMaterials.forRenderLayer(ctx.renderLayer());
		ChunkModelBuilder meshBuilder = buffers.get(material);
		ColorProvider<BlockState> colorizer = blockRenderer.getColorProviderRegistry().getColorProvider(ctx.state().getBlock());
		Vec3 offset = state.getOffset(world, pos);
		if (yOffset != 0) {
			offset = offset.add(0, yOffset, 0);
			cullSides = false;
		}
		LightPipeline lighter = blockRenderer.getLighters().getLighter(blockRenderer.callGetLightingMode(state, model, world, pos, layer));
		boolean rendered = false;
		for (Direction dir : DirectionUtil.ALL_DIRECTIONS) {
			random.setSeed(ctx.seed());
			List<BakedQuad> sided = model.getQuads(state, dir, random, modelData, layer);
			if (sided.isEmpty()) {
				continue;
			}
			if (!cullSides || blockRenderer.getOcclusionCache().shouldDrawSide(state, world, pos, dir)) {
				blockRenderer.callRenderQuadList(ctx, material, lighter, colorizer, offset, meshBuilder, sided, dir);
				rendered = true;
			}
		}

		random.setSeed(ctx.seed());
		List<BakedQuad> all = model.getQuads(state, null, random, modelData, layer);
		if (!all.isEmpty()) {
			blockRenderer.callRenderQuadList(ctx, material, lighter, colorizer, offset, meshBuilder, all, null);
			rendered = true;
		}

		return rendered;
	}

}
