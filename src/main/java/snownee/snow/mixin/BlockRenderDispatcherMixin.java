package snownee.snow.mixin;

import java.util.Optional;
import java.util.Random;

import org.spongepowered.asm.mixin.Final;
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

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {

	@Shadow
	@Final
	private ModelBlockRenderer modelRenderer;

	private final Options defaultOptions = new Options();
	private BakedModel cachedSnowModel;
	private BakedModel cachedOverlayModel;

	@Inject(
			method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLjava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z", at = @At(
				"HEAD"
			), remap = false, cancellable = true
	)
	private void srm_renderBatched(BlockState blockStateIn, BlockPos posIn, BlockAndTintGetter lightReaderIn, PoseStack matrixStackIn, VertexConsumer vertexBuilderIn, boolean checkSides, Random rand, IModelData modelData, CallbackInfoReturnable<Boolean> ci) {
		if (!(blockStateIn.getBlock() instanceof SnowVariant)) {
			return;
		}
		if (blockStateIn.hasProperty(SnowLayerBlock.LAYERS) && blockStateIn.getValue(SnowLayerBlock.LAYERS) == 8) {
			return;
		}
		BlockState state = modelData.getData(SnowBlockEntity.BLOCKSTATE);
		if (state == null || (!state.isAir() && state.getRenderShape() != RenderShape.MODEL)) {
			return;
		}
		try {
			RenderType cutoutMipped = RenderType.cutoutMipped();
			RenderType solid = RenderType.solid();
			RenderType layer = MinecraftForgeClient.getRenderType();
			boolean canRender;
			boolean ret = false;
			Block blockIn = blockStateIn.getBlock();
			Options options = Optional.ofNullable(modelData.getData(SnowBlockEntity.OPTIONS)).orElse(defaultOptions);
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
					String namespace = state.getBlock().getRegistryName().getNamespace();
					if ("projectvibrantjourneys".equals(namespace) || "foragecraft".equals(namespace)) {
						if (blockStateIn.getValue(SnowLayerBlock.LAYERS) > 3) {
							matrixStackIn.popPose();
							return;
						}
						matrixStackIn.translate(0.001, 0.101, 0.001);
						matrixStackIn.scale(0.998f, 1, 0.998f);
					}
				}
				ret |= modelRenderer.tesselateBlock(lightReaderIn, getBlockModel(state), state, posIn, matrixStackIn, vertexBuilderIn, false, rand, state.getSeed(posIn), OverlayTexture.NO_OVERLAY, modelData);
				matrixStackIn.popPose();
			}

			if (options.renderBottom && (layer == null || layer == solid)) {
				if (cachedSnowModel == null) {
					cachedSnowModel = getBlockModel(CoreModule.BLOCK.defaultBlockState());
				}
				ret |= modelRenderer.tesselateBlock(lightReaderIn, cachedSnowModel, CoreModule.BLOCK.defaultBlockState(), posIn, matrixStackIn, vertexBuilderIn, false, rand, state.getSeed(posIn), OverlayTexture.NO_OVERLAY, modelData);
			}

			if (blockStateIn.is(CoreModule.SLAB) || blockIn instanceof SnowLayerBlock) {
				if (options.renderOverlay && layer == cutoutMipped) {
					if (cachedOverlayModel == null) {
						cachedOverlayModel = Minecraft.getInstance().getModelManager().getModel(CoreModule.OVERLAY_MODEL);
					}
					matrixStackIn.pushPose();
					matrixStackIn.scale(1.002F, 1, 1.002F);
					BlockPos pos = posIn;
					if (blockStateIn.is(CoreModule.SLAB)) {
						matrixStackIn.translate(-0.001, -0.375, -0.001);
					} else {
						matrixStackIn.translate(-0.001, -1, -0.001);
						pos = pos.below();
					}
					ret |= modelRenderer.tesselateBlock(lightReaderIn, cachedOverlayModel, blockStateIn, pos, matrixStackIn, vertexBuilderIn, false, rand, blockStateIn.getSeed(pos), OverlayTexture.NO_OVERLAY, modelData);
					matrixStackIn.popPose();
				}
			} else {
				if (!options.renderOverlay) {
					ci.setReturnValue(ret);
					return;
				}
			}

			// specify base model's render type
			if (blockStateIn.is(CoreModule.TILE_BLOCK)) {
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
		cachedSnowModel = null;
		cachedOverlayModel = null;
	}
}
