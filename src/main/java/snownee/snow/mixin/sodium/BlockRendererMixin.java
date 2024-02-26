package snownee.snow.mixin.sodium;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowBlockEntity.Options;
import snownee.snow.client.SnowClient;
import snownee.snow.compat.sodium.RubidiumRenderAPI;
import snownee.snow.util.ClientProxy;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

	@Final
	@Shadow
	private RandomSource random;

	@Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
	private void srm_renderModel(BlockRenderContext ctx, ChunkBuildBuffers buffers, CallbackInfo ci) {
		if (!ClientProxy.shouldRedirect(ctx.state())) {
			return;
		}
		ModelData modelData = ctx.modelData();
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
		SnowClient.renderHook(
				ctx.world(),
				ctx.pos(),
				ctx.state(),
				camo,
				options,
				ctx.renderLayer(),
				() -> random,
				true,
				new RubidiumRenderAPI((BlockRendererAccess) this, ctx, buffers));
		ci.cancel();
	}
}
