
package snownee.snow.mixin.sodium;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowBlockEntity.Options;
import snownee.snow.client.SnowClient;
import snownee.snow.compat.sodium.RubidiumRenderAPI;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

	@Shadow
	private LightPipelineProvider lighters;
	@Shadow
	private BlockOcclusionCache occlusionCache;

	@Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
	private void srm_renderModel(BlockAndTintGetter lightReaderIn, BlockState blockStateIn, BlockPos posIn, BlockPos origin, BakedModel modelIn, ChunkModelBuilder buffers, boolean cull, long seed, ModelData modelData, @Nullable RenderType layer, RandomSource random, CallbackInfoReturnable<Boolean> ci) {
		if (!SnowClient.shouldRedirect(blockStateIn)) {
			return;
		}
		if (modelData == null) {
			modelData = ModelData.EMPTY;
		}
		BlockState camo = modelData.get(SnowBlockEntity.BLOCKSTATE);
		if (camo == null || camo.getRenderShape() != RenderShape.MODEL) {
			camo = Blocks.AIR.defaultBlockState();
		}
		Options options = modelData.get(SnowBlockEntity.OPTIONS);
		if (options == null) {
			options = SnowClient.fallbackOptions;
		}
		ci.setReturnValue(SnowClient.renderHook(lightReaderIn, posIn, blockStateIn, camo, options, layer, () -> random, cull, new RubidiumRenderAPI((BlockRendererAccess) this, modelData, seed, lighters, occlusionCache, origin, buffers)));
	}
}
