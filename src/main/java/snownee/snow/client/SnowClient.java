package snownee.snow.client;

import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.WatcherSnowVariant;
import snownee.snow.block.entity.SnowBlockEntity.Options;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowVariantModel;

public final class SnowClient {

	public static final Options fallbackOptions = new Options();
	public static BakedModel cachedSnowModel;
	public static BakedModel cachedOverlayModel;

	public static final ResourceLocation OVERLAY_MODEL = new ResourceLocation(SnowRealMagic.MODID, "block/overlay");

	public static final Map<ResourceLocation, ModelDefinition> snowVariantMapping = Maps.newLinkedHashMap();

	public static boolean shouldRedirect(BlockState state) {
		if (!(state.getBlock() instanceof SnowVariant)) {
			return false;
		}
		if (!state.hasBlockEntity()) {
			return false;
		}
		if (state.hasProperty(SnowLayerBlock.LAYERS) && state.getValue(SnowLayerBlock.LAYERS) == 8) {
			return false;
		}
		return true;
	}

	public static boolean renderHook(BlockAndTintGetter world, BlockPos pos, BlockState state, BlockState camo, Options options, @Nullable RenderType layer, Supplier<RandomSource> randomSupplier, boolean cullSides, RenderAPI api) {
		if (state.getBlock() instanceof WatcherSnowVariant) {
			if (layer == null || layer == RenderType.solid()) {
				((WatcherSnowVariant) state.getBlock()).updateOptions(state, world, pos, options);
			}
		}
		boolean rendered = false;
		boolean useVariant = false;
		BakedModel model;
		if (!camo.isAir() && camo.getRenderShape() == RenderShape.MODEL) {
			model = getBlockModel(camo);
			if (SnowClientConfig.snowVariants && model instanceof SnowVariantModel) {
				BakedModel snowVariant = ((SnowVariantModel) model).getSnowVariant();
				if (snowVariant != null) {
					model = snowVariant;
					useVariant = true;
				}
			}
			double yOffset = camo.is(CoreModule.OFFSET_Y) ? 0.101 : 0;
			rendered |= api.translateYAndRender(world, camo, pos, layer, randomSupplier, cullSides, model, yOffset);
		}
		if (options.renderBottom && (layer == null || layer == RenderType.solid())) {
			BlockState snow = state.getBlock() instanceof SnowVariant ? ((SnowVariant) state.getBlock()).getSnowState(state, world, pos) : Blocks.AIR.defaultBlockState();
			if (!snow.isAir()) {
				if (snow == Blocks.SNOW.defaultBlockState()) {
					if (cachedSnowModel == null) {
						cachedSnowModel = getBlockModel(snow);
					}
					model = cachedSnowModel;
				} else {
					model = getBlockModel(snow);
				}
				double yOffset = CoreModule.SLAB.is(state) ? 0.5 : 0;
				rendered |= api.translateYAndRender(world, snow, pos, layer, randomSupplier, cullSides, model, yOffset);
			}
		}
		if (options.renderOverlay && (layer == null || layer == RenderType.cutoutMipped()) && (!useVariant || CoreModule.TILE_BLOCK.is(state))) {
			BlockPos pos2 = pos;
			double yOffset;
			if (CoreModule.TILE_BLOCK.is(state) || CoreModule.SLAB.is(state)) {
				if (cachedOverlayModel == null) {
					cachedOverlayModel = getBlockModel(OVERLAY_MODEL);
				}
				model = cachedOverlayModel;
				if (CoreModule.SLAB.is(state)) {
					yOffset = -0.375;
				} else {
					yOffset = -1;
					pos2 = pos.below();
				}
			} else {
				yOffset = state.getBlock() instanceof SnowVariant ? (float) ((SnowVariant) state.getBlock()).getYOffset() : 0;
				model = getBlockModel(state);
			}
			rendered |= api.translateYAndRender(world, state, pos2, layer, randomSupplier, cullSides, model, yOffset);
		}
		return rendered;
	}

	private static BakedModel getBlockModel(BlockState state) {
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
	}

	private static BakedModel getBlockModel(ResourceLocation location) {
		return Minecraft.getInstance().getModelManager().getModel(location);
	}
}
