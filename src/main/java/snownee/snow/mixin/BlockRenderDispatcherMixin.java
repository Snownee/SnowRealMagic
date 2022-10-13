package snownee.snow.mixin;

import java.util.Optional;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.WatcherSnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowBlockEntity.Options;
import snownee.snow.client.ClientVariables;
import snownee.snow.client.SnowClientConfig;
import snownee.snow.client.model.SnowVariantModel;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {

	@Shadow
	private ModelBlockRenderer modelRenderer;

	@Inject(
			method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLjava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z", at = @At(
				"HEAD"
			), remap = false, cancellable = true
	)
	private void srm_renderBatched(BlockState blockStateIn, BlockPos posIn, BlockAndTintGetter lightReaderIn, PoseStack matrixStackIn, VertexConsumer vertexBuilderIn, boolean checkSides, Random random, IModelData modelData, CallbackInfoReturnable<Boolean> ci) {
		if (!(blockStateIn.getBlock() instanceof SnowVariant)) {
			return;
		}
		if (!blockStateIn.hasBlockEntity()) {
			return;
		}
		if (blockStateIn.hasProperty(SnowLayerBlock.LAYERS) && blockStateIn.getValue(SnowLayerBlock.LAYERS) == 8) {
			return;
		}
		BlockState state = modelData.getData(SnowBlockEntity.BLOCKSTATE);
		if (state == null || (!state.isAir() && state.getRenderShape() != RenderShape.MODEL)) {
			return;
		}
		boolean useSnowVariant = false;
		try {
			RenderType cutoutMipped = RenderType.cutoutMipped();
			RenderType solid = RenderType.solid();
			RenderType layer = MinecraftForgeClient.getRenderType();
			boolean canRender;
			boolean ret = false;
			Block blockIn = blockStateIn.getBlock();
			Options options = Optional.ofNullable(modelData.getData(SnowBlockEntity.OPTIONS)).orElse(ClientVariables.fallbackOptions);
			if (layer == null) {
				canRender = layer == cutoutMipped;
			} else {
				if (layer == solid && blockIn instanceof WatcherSnowVariant) { // solid is the first rendertype
					((WatcherSnowVariant) blockIn).updateOptions(blockStateIn, lightReaderIn, posIn, options);
				}
				canRender = ItemBlockRenderTypes.canRenderInLayer(state, layer);
			}
			if (canRender && !state.isAir()) {
				matrixStackIn.pushPose();
				if (blockStateIn.hasProperty(SnowLayerBlock.LAYERS)) {
					if (state.is(CoreModule.OFFSET_Y)) {
						if (blockStateIn.getValue(SnowLayerBlock.LAYERS) > 3) {
							matrixStackIn.popPose();
							return;
						}
						matrixStackIn.translate(0.001, 0.101, 0.001);
						matrixStackIn.scale(0.998f, 1, 0.998f);
					}
				}
				BakedModel model = getBlockModel(state);
				if (SnowClientConfig.snowVariants && model instanceof SnowVariantModel) {
					BakedModel snowVariant = ((SnowVariantModel) model).getSnowVariant();
					if (snowVariant != null) {
						model = snowVariant;
						useSnowVariant = true;
					}
				}
				ret |= modelRenderer.tesselateBlock(lightReaderIn, model, state, posIn, matrixStackIn, vertexBuilderIn, false, random, state.getSeed(posIn), OverlayTexture.NO_OVERLAY, modelData);
				matrixStackIn.popPose();
			}

			if (options.renderBottom && (layer == null || layer == solid)) {
				if (ClientVariables.cachedSnowModel == null) {
					ClientVariables.cachedSnowModel = getBlockModel(CoreModule.BLOCK.defaultBlockState());
				}
				ret |= modelRenderer.tesselateBlock(lightReaderIn, ClientVariables.cachedSnowModel, CoreModule.BLOCK.defaultBlockState(), posIn, matrixStackIn, vertexBuilderIn, false, random, state.getSeed(posIn), OverlayTexture.NO_OVERLAY, modelData);
			}

			boolean slab = CoreModule.SLAB.is(blockStateIn);
			if (slab || CoreModule.TILE_BLOCK.is(blockStateIn)) {
				if (options.renderOverlay && layer == cutoutMipped) {
					if (ClientVariables.cachedOverlayModel == null) {
						ClientVariables.cachedOverlayModel = Minecraft.getInstance().getModelManager().getModel(ClientVariables.OVERLAY_MODEL);
					}
					matrixStackIn.pushPose();
					BlockPos pos = posIn;
					if (slab) {
						matrixStackIn.translate(0, -0.375, 0);
					} else {
						matrixStackIn.translate(0, -1, 0);
						pos = pos.below();
					}
					ret |= modelRenderer.tesselateBlock(lightReaderIn, ClientVariables.cachedOverlayModel, blockStateIn, pos, matrixStackIn, vertexBuilderIn, false, random, blockStateIn.getSeed(pos), OverlayTexture.NO_OVERLAY, modelData);
					matrixStackIn.popPose();
				}
			} else {
				if (!options.renderOverlay || useSnowVariant) {
					ci.setReturnValue(ret);
					return;
				}
			}

			// specify base model's render type
			if (CoreModule.TILE_BLOCK.is(blockStateIn)) {
				if (layer != solid) {
					ci.setReturnValue(ret);
					return;
				}
			} else {
				if (layer != cutoutMipped) {
					ci.setReturnValue(ret);
					return;
				}
			}

			double yOffset = ((SnowVariant) blockIn).getYOffset();
			if (yOffset != 0) {
				matrixStackIn.translate(0, yOffset, 0);
			}
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block in world");
			CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
			CrashReportCategory.populateBlockDetails(crashreportcategory, lightReaderIn, posIn, blockStateIn);
			throw new ReportedException(crashreport);
		}
	}

	@Shadow
	public abstract BakedModel getBlockModel(BlockState state);

	@Inject(at = @At("HEAD"), method = "onResourceManagerReload")
	private void srm_onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci) {
		ClientVariables.cachedSnowModel = null;
		ClientVariables.cachedOverlayModel = null;
	}
}
