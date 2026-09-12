package snownee.snow.client;

import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity.Options;
import snownee.snow.client.model.ModelMetadataSection;
import snownee.snow.util.ClientProxy;

public final class ClientHooks {

	public static @Nullable BlockStateModel cachedSnowModel;
	public static @Nullable BlockStateModel cachedOverlayModel;

	public static final Identifier OVERLAY_MODEL = SnowRealMagic.id("block/overlay");

	public static final Map<Identifier, ModelMetadataSection> snowVariantMapping = Maps.newLinkedHashMap();
	public static final Set<Block> overrideBlocks = Sets.newHashSet();
	public static final Set<Block> requiredOverrideBlocks = Sets.newHashSet();

	@CanIgnoreReturnValue
	public static boolean renderHook(
			BlockState blockState,
			BlockState camo,
			Options options,
			@Nullable ChunkSectionLayer renderType,
			RenderAPI api) {
		boolean rendered = false;
		SnowVariant snowVariant = (SnowVariant) blockState.getBlock();

		boolean full = blockState.hasProperty(SnowLayerBlock.LAYERS) && blockState.getValue(SnowLayerBlock.LAYERS) == 8 && !camo.is(
				CoreModule.EXPAND_MODEL);
		if (!full && !camo.isAir() && camo.getRenderShape() == RenderShape.MODEL) {
			boolean useVariant = (SnowClientConfig.snowVariants || requiredOverrideBlocks.contains(camo.getBlock())) && overrideBlocks.contains(camo.getBlock());
			double yOffset = isOffsetY(camo) ? 0.101 : 0;
			rendered |= api.render(camo, ClientProxy.getBlockModel(camo), yOffset, RenderAPI.ModelPart.CAMO);

			if (!useVariant && (renderType == null || renderType == ChunkSectionLayer.CUTOUT) && snowVariant.srm$canRenderDecoration(
					blockState)) {
				rendered |= api.render(
						blockState,
						ClientProxy.getBlockModel(blockState),
						yOffset + snowVariant.srm$renderDecorationOffset(blockState),
						RenderAPI.ModelPart.DECORATION);
			}
		}

		BlockState snow = snowVariant.srm$getSnowState(blockState, api.level(), api.pos());
		if (!snow.isAir()) {
			BlockStateModel model;
			if (snow == Blocks.SNOW.defaultBlockState()) {
				if (cachedSnowModel == null) {
					cachedSnowModel = ClientProxy.getBlockModel(snow);
				}
				model = cachedSnowModel;
			} else {
				model = ClientProxy.getBlockModel(snow);
			}
			rendered |= api.render(snow, model, snowVariant.srm$renderLayerOffset(blockState), RenderAPI.ModelPart.SNOW_LAYER);
		}

		if (options.renderOverlay && (renderType == null || renderType == ChunkSectionLayer.CUTOUT) && snowVariant.srm$canRenderOverlay(
				blockState)) {
			if (cachedOverlayModel == null) {
				cachedOverlayModel = ClientProxy.getExtraBlockModel(ClientProxy.OVERLAY_MODEL);
			}
			double yOffset = snowVariant.srm$renderLayerOffset(blockState) - 1.0;
			if (snowVariant.srm$layers(blockState, api.level(), api.pos()) == 8) {
				yOffset -= 0.002;
			}
			rendered |= api.render(blockState, cachedOverlayModel, yOffset, RenderAPI.ModelPart.SNOW_OVERLAY);
		}
		return rendered;
	}

	public static boolean isOffsetY(BlockState blockState) {
		return blockState.getBlock() instanceof FlowerBedBlock || blockState.is(CoreModule.OFFSET_Y);
	}

}
