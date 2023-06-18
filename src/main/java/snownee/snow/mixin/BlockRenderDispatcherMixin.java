package snownee.snow.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowBlockEntity.Options;
import snownee.snow.client.ForgeHookRenderAPI;
import snownee.snow.client.SnowClient;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {

	@Inject(
			method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At(
				"HEAD"
			), remap = false, cancellable = true
	)
	private void srm_renderBatched(BlockState blockStateIn, BlockPos posIn, BlockAndTintGetter lightReaderIn, PoseStack matrixStackIn, VertexConsumer vertexBuilderIn, boolean checkSides, RandomSource random, ModelData modelData, @Nullable RenderType layer, CallbackInfo ci) {
		if (!SnowClient.shouldRedirect(blockStateIn)) {
			return;
		}
		BlockState camo = modelData.get(SnowBlockEntity.BLOCKSTATE);
		if (camo == null || camo.getRenderShape() != RenderShape.MODEL) {
			camo = Blocks.AIR.defaultBlockState();
		}
		Options options = modelData.get(SnowBlockEntity.OPTIONS);
		if (options == null) {
			options = SnowClient.fallbackOptions;
		}
		SnowClient.renderHook(lightReaderIn, posIn, blockStateIn, camo, options, layer, () -> random, checkSides, new ForgeHookRenderAPI(modelData, matrixStackIn, vertexBuilderIn));
		ci.cancel();
	}

	@Inject(at = @At("HEAD"), method = "onResourceManagerReload")
	private void srm_onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci) {
		SnowClient.cachedSnowModel = null;
		SnowClient.cachedOverlayModel = null;
	}
}
