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
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowTile;
import snownee.snow.block.SnowTile.Options;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.WatcherSnowVariant;
import snownee.snow.client.ClientVariables;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

	@Shadow
	private LightPipelineProvider lighters;
	@Shadow
	private Random random;
	@Shadow
	private BlockOcclusionCache occlusionCache;

	@SuppressWarnings("deprecation")
	@Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
	private void srm_renderModel(IBlockDisplayReader lightReaderIn, BlockState blockStateIn, BlockPos posIn, IBakedModel model, ChunkModelBuffers buffers, boolean cull, long seed, IModelData modelData, CallbackInfoReturnable<Boolean> ci) {
		if (!(blockStateIn.getBlock() instanceof SnowVariant)) {
			return;
		}
		if (!blockStateIn.hasTileEntity()) {
			return;
		}
		if (blockStateIn.hasProperty(SnowBlock.LAYERS) && blockStateIn.get(SnowBlock.LAYERS) == 8) {
			return;
		}
		BlockState state = modelData.getData(SnowTile.BLOCKSTATE);
		if (state == null || (!state.isAir() && state.getRenderType() != BlockRenderType.MODEL)) {
			return;
		}
		try {
			RenderType cutoutMipped = RenderType.getCutoutMipped();
			RenderType solid = RenderType.getSolid();
			RenderType layer = MinecraftForgeClient.getRenderLayer();
			boolean canRender;
			boolean ret = false;
			Block blockIn = blockStateIn.getBlock();
			Options options = Optional.ofNullable(modelData.getData(SnowTile.OPTIONS)).orElse(ClientVariables.fallbackOptions);
			if (layer == null) {
				canRender = layer == cutoutMipped;
			} else {
				if (layer == solid && blockIn instanceof WatcherSnowVariant) { // solid is the first rendertype
					((WatcherSnowVariant) blockIn).updateOptions(blockStateIn, lightReaderIn, posIn, options);
				}
				canRender = RenderTypeLookup.canRenderInLayer(state, layer);
			}
			double yOffset = 0;
			if (canRender && !state.isAir()) {
				if (blockStateIn.hasProperty(SnowBlock.LAYERS)) {
					String namespace = state.getBlock().getRegistryName().getNamespace();
					if ("projectvibrantjourneys".equals(namespace) || "foragecraft".equals(namespace)) {
						if (blockStateIn.get(SnowBlock.LAYERS) > 3) {
							return;
						}
						yOffset = 0.1;
					}
				}
				ret |= renderModelWithYOffset(lightReaderIn, state, posIn, getBlockModel(state), buffers, cull, seed, modelData, yOffset);
			}

			if (options.renderBottom && (layer == null || layer == solid)) {
				if (ClientVariables.cachedSnowModel == null) {
					ClientVariables.cachedSnowModel = getBlockModel(CoreModule.BLOCK.getDefaultState());
				}
				ret |= renderModel(lightReaderIn, CoreModule.BLOCK.getDefaultState(), posIn, ClientVariables.cachedSnowModel, buffers, cull, seed, modelData);
			}

			boolean slab = blockIn.matchesBlock(CoreModule.SLAB);
			if (slab || blockIn instanceof SnowBlock) {
				if (options.renderOverlay && layer == cutoutMipped) {
					if (ClientVariables.cachedOverlayModel == null) {
						ClientVariables.cachedOverlayModel = Minecraft.getInstance().getModelManager().getModel(CoreModule.OVERLAY_MODEL);
					}
					BlockPos pos = posIn;
					if (slab) {
						yOffset = -0.375;
					} else {
						yOffset = -1;
						pos = pos.down();
					}
					ret |= renderModelWithYOffset(lightReaderIn, CoreModule.BLOCK.getDefaultState(), posIn, ClientVariables.cachedOverlayModel, buffers, cull, seed, modelData, yOffset);
				}
			} else {
				if (!options.renderOverlay) {
					ci.setReturnValue(ret);
					return;
				}
			}

			// specify base model's render type
			if (blockStateIn.isIn(CoreModule.TILE_BLOCK)) {
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
			ret |= renderModelWithYOffset(lightReaderIn, blockStateIn, posIn, model, buffers, cull, seed, modelData, yOffset);
			ci.setReturnValue(ret);
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
			CrashReportCategory.addBlockInfo(crashreportcategory, posIn, blockStateIn);
			throw new ReportedException(crashreport);
		}
	}

	private IBakedModel getBlockModel(BlockState state) {
		return Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
	}

	public boolean renderModelWithYOffset(IBlockDisplayReader world, BlockState state, BlockPos pos, IBakedModel model, ChunkModelBuffers buffers, boolean cull, long seed, IModelData modelData, double yOffset) {
		Vector3d offset = state.getOffset(world, pos);
		if (yOffset != 0) {
			offset = offset.add(0, yOffset, 0);
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
				this.renderQuadList(world, state, pos, lighter, offset, buffers, sided, dir);

				rendered = true;
			}
		}

		this.random.setSeed(seed);

		List<BakedQuad> all = model.getQuads(state, null, this.random, modelData);

		if (!all.isEmpty()) {
			this.renderQuadList(world, state, pos, lighter, offset, buffers, all, null);

			rendered = true;
		}

		return rendered;
	}

	@Shadow
	abstract boolean renderModel(IBlockDisplayReader world, BlockState state, BlockPos pos, IBakedModel model, ChunkModelBuffers buffers, boolean cull, long seed, IModelData modelData);

	@Shadow
	abstract LightMode getLightingMode(BlockState state, IBakedModel model);

	@Shadow
	abstract void renderQuadList(IBlockDisplayReader world, BlockState state, BlockPos pos, LightPipeline lighter, Vector3d offset, ChunkModelBuffers buffers, List<BakedQuad> quads, Direction facing);
}
