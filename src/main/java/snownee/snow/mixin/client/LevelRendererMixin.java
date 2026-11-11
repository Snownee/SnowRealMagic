package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.util.ClientProxy;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	
	/**
	 * The destroy overlay is rendered from the model of the vanilla block state, which for snow-covered blocks is only
	 * the snow layer or even an empty model. Render it from the model that is actually visible instead.
	 */
	@WrapOperation(
			method = "submitBlockDestroyAnimation",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/block/BlockStateModelSet;get(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;"))
	private BlockStateModel srm_getDestroyModel(
			BlockStateModelSet modelSet,
			BlockState state,
			Operation<BlockStateModel> original,
			@Local(name = "pos") BlockPos pos) {
		BlockStateModel model = original.call(modelSet, state);
		if (!(state.getBlock() instanceof SnowVariant snow)) {
			return model;
		}
		var level = Minecraft.getInstance().level;
		if (level == null) {
			return model;
		}
		BlockState camo = getCamo(level, pos);
		boolean full = isFull(state, camo);
		if (!full && isCamoVisible(camo)) {
			return ClientProxy.getUnwrappedBlockModel(camo);
		}
		BlockState snowState = snow.srm$getSnowState(state, level, pos);
		if (!snowState.isAir()) {
			return ClientProxy.getUnwrappedBlockModel(snowState);
		}
		if (isCamoVisible(camo)) {
			return ClientProxy.getUnwrappedBlockModel(camo);
		}
		return model;
	}

	/**
	 * When the camo is rendered, the snow layer/decoration on top of it is visible as well. Submit a second destroy
	 * overlay for those quads so the snow part gets cracks too.
	 */
	@WrapOperation(
			method = "submitBlockDestroyAnimation",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitBreakingBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;JI)V"))
	private void srm_submitSnowLayerCracks(
			SubmitNodeCollector collector,
			PoseStack poseStack,
			BlockStateModel model,
			long seed,
			int progress,
			Operation<Void> original,
			@Local(name = "state") BlockBreakingRenderState breakingState,
			@Local(name = "pos") BlockPos pos) {
		original.call(collector, poseStack, model, seed, progress);
		BlockState state = breakingState.blockState();
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || !(state.getBlock() instanceof SnowVariant snow)) {
			return;
		}
		BlockState camo = getCamo(level, pos);
		if (isFull(state, camo) || !isCamoVisible(camo)) {
			return;
		}
		if (snow.srm$canRenderDecoration(state)) {
			srm$submitExtra(
					collector,
					poseStack,
					ClientProxy.getUnwrappedBlockModel(state),
					seed,
					progress,
					snow.srm$renderDecorationOffset(state) + 0.001,
					original);
		}
		BlockState snowState = snow.srm$getSnowState(state, level, pos);
		// Blocks like slabs draw the snow layer with a render-time quad transform, so the destroy overlay has to be
		// submitted with the very same offset, otherwise the cracks end up inside the block instead of on the snow.
		if (!snowState.isAir()) {
			srm$submitExtra(
					collector,
					poseStack,
					ClientProxy.getUnwrappedBlockModel(snowState),
					seed,
					progress,
					snow.srm$renderLayerOffset(state) + 0.001,
					original);
		}
	}

	private static void srm$submitExtra(
			SubmitNodeCollector collector,
			PoseStack poseStack,
			BlockStateModel model,
			long seed,
			int progress,
			double yOffset,
			Operation<Void> original) {
		if (yOffset != 0) {
			poseStack.pushPose();
			poseStack.translate(0, yOffset, 0);
			original.call(collector, poseStack, model, seed, progress);
			poseStack.popPose();
		} else {
			original.call(collector, poseStack, model, seed, progress);
		}
	}

	private static BlockState getCamo(ClientLevel level, BlockPos pos) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		return blockEntity instanceof SnowBlockEntity snowBlockEntity ? snowBlockEntity.getContainedState() : null;
	}

	private static boolean isFull(BlockState state, BlockState camo) {
		return state.hasProperty(SnowLayerBlock.LAYERS) && state.getValue(SnowLayerBlock.LAYERS) == 8 &&
				(camo == null || !camo.is(CoreModule.EXPAND_MODEL));
	}

	private static boolean isCamoVisible(BlockState camo) {
		return camo != null && !camo.isAir() && camo.getRenderShape() == RenderShape.MODEL;
	}
}
