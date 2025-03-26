package snownee.snow.client;

import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity.Options;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.util.ClientProxy;

@NotNullByDefault
public final class SnowClient {

	public static BakedModel cachedSnowModel;
	public static BakedModel cachedOverlayModel;

	public static final ResourceLocation OVERLAY_MODEL = SnowRealMagic.id("block/overlay");

	public static final Map<ResourceLocation, ModelDefinition> snowVariantMapping = Maps.newLinkedHashMap();
	public static final Set<Block> overrideBlocks = Sets.newHashSet();

	@CanIgnoreReturnValue
	public static boolean renderHook(
			BlockAndTintGetter level,
			BlockPos pos,
			BlockState blockState,
			BlockState camo,
			Options options,
			@Nullable RenderType renderType,
			boolean cullSides,
			RenderAPI api) {
		boolean rendered = false;
		SnowVariant snowVariant = (SnowVariant) blockState.getBlock();

		boolean full = blockState.hasProperty(SnowLayerBlock.LAYERS) && blockState.getValue(SnowLayerBlock.LAYERS) == 8;
		if (!full && !camo.isAir() && camo.getRenderShape() == RenderShape.MODEL) {
			boolean useVariant = false;
			if (SnowClientConfig.snowVariants && overrideBlocks.contains(camo.getBlock())) {
				useVariant = true;
			}
			double yOffset = camo.is(CoreModule.OFFSET_Y) ? 0.101 : 0;
			rendered |= api.render(camo, pos, cullSides, ClientProxy.getBlockModel(camo), yOffset, RenderAPI.ModelPart.CAMO);

			if (!useVariant && (renderType == null || renderType == RenderType.cutoutMipped()) &&
					snowVariant.srm$canRenderDecoration(blockState)) {
				rendered |= api.render(
						blockState,
						pos,
						cullSides,
						ClientProxy.getBlockModel(blockState),
						yOffset + snowVariant.srm$renderDecorationOffset(blockState),
						RenderAPI.ModelPart.DECORATION);
			}
		}

		BlockState snow = snowVariant.srm$getSnowState(blockState, level, pos);
		if (!snow.isAir() && (renderType == null || renderType == RenderType.solid())) {
			BakedModel model;
			if (snow == Blocks.SNOW.defaultBlockState()) {
				if (cachedSnowModel == null) {
					cachedSnowModel = ClientProxy.getBlockModel(snow);
				}
				model = cachedSnowModel;
			} else {
				model = ClientProxy.getBlockModel(snow);
			}
			rendered |= api.render(
					snow,
					pos,
					cullSides,
					model,
					snowVariant.srm$renderLayerOffset(blockState),
					RenderAPI.ModelPart.SNOW_LAYER);
		}

		if (options.renderOverlay && (renderType == null || renderType == RenderType.cutoutMipped()) &&
				snowVariant.srm$canRenderOverlay(blockState)) {
			BlockPos pos2 = pos;
			double yOffset;
			if (cachedOverlayModel == null) {
				cachedOverlayModel = ClientProxy.getBlockModel(OVERLAY_MODEL);
			}
			yOffset = snowVariant.srm$renderLayerOffset(blockState) - 1.0;
			if (yOffset <= -1) {
				pos2 = pos.below();
			}
			if (snowVariant.srm$layers(blockState, level, pos) == 8) {
				yOffset -= 0.002;
			}
			rendered |= api.render(blockState, pos2, cullSides, cachedOverlayModel, yOffset, RenderAPI.ModelPart.SNOW_OVERLAY);
		}
		return rendered;
	}

}
