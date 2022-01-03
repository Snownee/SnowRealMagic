/*
package snownee.snow.mixin.sodium;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import snownee.snow.ClientVariables;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.WatcherSnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.block.entity.SnowBlockEntity.Options;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

	@Shadow
	private LightPipelineProvider lighters;
	@Shadow
	private Random random;
	@Shadow
	private BlockOcclusionCache occlusionCache;

	@Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
	private void srm_renderModel(BlockAndTintGetter lightReaderIn, BlockState blockStateIn, BlockPos posIn, BlockPos origin, BakedModel model, ChunkModelBuilder buffers, boolean cull, long seed, CallbackInfoReturnable<Boolean> ci) {
		if (!(blockStateIn.getBlock() instanceof SnowVariant)) {
			return;
		}
		if (!blockStateIn.hasBlockEntity()) {
			return;
		}
		if (blockStateIn.hasProperty(SnowLayerBlock.LAYERS) && blockStateIn.getValue(SnowLayerBlock.LAYERS) == 8) {
			return;
		}
		IModelData modelData = ModelDataManager.getModelData(Minecraft.getInstance().level, posIn);
		if (modelData == null) {
			modelData = EmptyModelData.INSTANCE;
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
			Options options = Optional.ofNullable(modelData.getData(SnowBlockEntity.OPTIONS)).orElse(ClientVariables.fallbackOptions);
			if (layer == null) {
				canRender = layer == cutoutMipped;
			} else {
				if (layer == solid && blockIn instanceof WatcherSnowVariant) { // solid is the first rendertype
					((WatcherSnowVariant) blockIn).updateOptions(blockStateIn, lightReaderIn, posIn, options);
				}
				canRender = ItemBlockRenderTypes.canRenderInLayer(state, layer);
			}
			double yOffset = 0;
			if (canRender && !state.isAir()) {
				if (blockStateIn.hasProperty(SnowLayerBlock.LAYERS)) {
					String namespace = state.getBlock().getRegistryName().getNamespace();
					if ("projectvibrantjourneys".equals(namespace) || "foragecraft".equals(namespace)) {
						if (blockStateIn.getValue(SnowLayerBlock.LAYERS) > 3) {
							return;
						}
						yOffset = 0.1;
					}
				}
				ret |= renderModelWithYOffset(lightReaderIn, state, posIn, origin, getBlockModel(state), buffers, cull, seed, modelData, yOffset);
			}

			if (options.renderBottom && (layer == null || layer == solid)) {
				if (ClientVariables.cachedSnowModel == null) {
					ClientVariables.cachedSnowModel = getBlockModel(CoreModule.BLOCK.defaultBlockState());
				}
				ret |= renderModel(lightReaderIn, CoreModule.BLOCK.defaultBlockState(), posIn, origin, ClientVariables.cachedSnowModel, buffers, cull, seed);
			}

			if (blockStateIn.is(CoreModule.SLAB) || blockIn instanceof SnowLayerBlock) {
				if (options.renderOverlay && layer == cutoutMipped) {
					if (ClientVariables.cachedOverlayModel == null) {
						ClientVariables.cachedOverlayModel = Minecraft.getInstance().getModelManager().getModel(CoreModule.OVERLAY_MODEL);
					}
					BlockPos pos = posIn;
					if (blockStateIn.is(CoreModule.SLAB)) {
						yOffset = -0.375;
					} else {
						yOffset = -1;
						pos = pos.below();
					}
					ret |= renderModelWithYOffset(lightReaderIn, CoreModule.BLOCK.defaultBlockState(), posIn, origin, ClientVariables.cachedOverlayModel, buffers, cull, seed, modelData, yOffset);
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

			yOffset = ((SnowVariant) blockIn).getYOffset();
			ret |= renderModelWithYOffset(lightReaderIn, blockStateIn, posIn, origin, model, buffers, cull, seed, modelData, yOffset);
			ci.setReturnValue(ret);
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block in world");
			CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
			CrashReportCategory.populateBlockDetails(crashreportcategory, lightReaderIn, posIn, blockStateIn);
			throw new ReportedException(crashreport);
		}
	}

	private BakedModel getBlockModel(BlockState state) {
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
	}

	public boolean renderModelWithYOffset(BlockAndTintGetter world, BlockState state, BlockPos pos, BlockPos origin, BakedModel model, ChunkModelBuilder buffers, boolean cull, long seed, IModelData modelData, double yOffset) {
		Vec3 offset = Vec3.ZERO;
		if (yOffset != 0) {
			offset = new Vec3(0, yOffset, 0);
		}
		LightPipeline lighter = this.lighters.getLighter(this.getLightingMode(state, model));

		boolean rendered = false;

		for (Direction dir : DirectionUtil.ALL_DIRECTIONS) {
			this.random.setSeed(seed);

			List<BakedQuad> sided = model.getQuads(state, dir, this.random, modelData);

			if (sided.isEmpty()) {
				continue;
			}

			if (!cull || this.occlusionCache.shouldDrawSide(state, world, pos, dir)) {
				this.renderQuadList(world, state, pos, origin, lighter, offset, buffers, sided, ModelQuadFacing.fromDirection(dir));

				rendered = true;
			}
		}

		this.random.setSeed(seed);

		List<BakedQuad> all = model.getQuads(state, null, this.random, modelData);

		if (!all.isEmpty()) {
			this.renderQuadList(world, state, pos, origin, lighter, offset, buffers, all, ModelQuadFacing.UNASSIGNED);

			rendered = true;
		}

		return rendered;
	}

	@Shadow
	abstract boolean renderModel(BlockAndTintGetter world, BlockState state, BlockPos pos, BlockPos origin, BakedModel model, ChunkModelBuilder buffers, boolean cull, long seed);

	@Shadow
	abstract LightMode getLightingMode(BlockState state, BakedModel model);

	@Shadow
	abstract void renderQuadList(BlockAndTintGetter world, BlockState state, BlockPos pos, BlockPos origin, LightPipeline lighter, Vec3 offset, ChunkModelBuilder buffers, List<BakedQuad> quads, ModelQuadFacing facing);
}
*/