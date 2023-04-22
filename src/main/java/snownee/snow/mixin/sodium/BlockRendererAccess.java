package snownee.snow.mixin.sodium;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
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
	LightMode callGetLightingMode(BlockState state, BakedModel model, BlockAndTintGetter world, BlockPos pos);

	@Invoker
	void callRenderQuadList(BlockRenderContext blockRenderContext, LightPipeline lighter, Vec3 offset, ChunkModelBuilder buffers, List<BakedQuad> sided, Direction dir);

	@Accessor
	LightPipelineProvider getLighters();

	@Accessor
	BlockOcclusionCache getOcclusionCache();

}
