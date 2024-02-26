package snownee.snow.mixin.sodium;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.color.ColorProviderRegistry;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(value = BlockRenderer.class, remap = false)
public interface BlockRendererAccess {

	@Invoker
	LightMode callGetLightingMode(BlockState state, BakedModel model, BlockAndTintGetter world, BlockPos pos, RenderType layer);

	@Invoker
	void callRenderQuadList(
			BlockRenderContext ctx,
			Material material,
			LightPipeline lighter,
			ColorProvider<BlockState> colorizer,
			Vec3 offset,
			ChunkModelBuilder builder,
			List<BakedQuad> quads,
			Direction cullFace);

	@Accessor
	LightPipelineProvider getLighters();

	@Accessor
	BlockOcclusionCache getOcclusionCache();

	@Accessor
	ColorProviderRegistry getColorProviderRegistry();

}
