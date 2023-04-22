package snownee.snow.mixin.sodium;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
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

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

	@Final
	@Shadow
	private RandomSource random;

	@Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
	private void srm_renderModel(BlockRenderContext ctx, ChunkModelBuilder buffers, CallbackInfoReturnable<Boolean> ci) {
		if (!SnowClient.shouldRedirect(ctx.state())) {
			return;
		}
		ModelData modelData = ctx.data();
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
		ci.setReturnValue(SnowClient.renderHook(ctx.world(), ctx.pos(), ctx.state(), camo, options, ctx.layer(), () -> random, true, new RubidiumRenderAPI((BlockRendererAccess) this, ctx, buffers)));
	}
}
