package snownee.snow.client;

import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.WatcherSnowVariant;
import snownee.snow.block.entity.SnowBlockEntity.Options;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowVariantModel;
import snownee.snow.util.ClientProxy;

public final class SnowClient {

	public static final Options fallbackOptions = new Options();
	public static BakedModel cachedSnowModel;
	public static BakedModel cachedOverlayModel;

	public static final ResourceLocation OVERLAY_MODEL = new ResourceLocation(SnowRealMagic.MODID, "block/overlay");

	public static final Map<ResourceLocation, ModelDefinition> snowVariantMapping = Maps.newLinkedHashMap();

	public static boolean renderHook(BlockAndTintGetter world, BlockPos pos, BlockState state, BlockState camo, Options options, @Nullable RenderType layer, Supplier<RandomSource> randomSupplier, boolean cullSides, RenderAPI api) {
		if (layer == null || layer == RenderType.solid()) {
			if (state.getBlock() instanceof WatcherSnowVariant watcher) {
				//FIXME find out if still necessary
				watcher.updateOptions(state, world, pos, options);
			}
		}
		boolean rendered = false;
		boolean useVariant = false;
		BakedModel model;
		if (!camo.isAir() && camo.getRenderShape() == RenderShape.MODEL) {
			model = ClientProxy.getBlockModel(camo);
			if (SnowClientConfig.snowVariants && model instanceof SnowVariantModel) {
				BakedModel variantModel = ((SnowVariantModel) model).srm$getSnowVariant();
				if (variantModel != null) {
					model = variantModel;
					useVariant = true;
				}
			}
			double yOffset = camo.is(CoreModule.OFFSET_Y) ? 0.101 : 0;
			rendered |= api.translateYAndRender(world, camo, pos, layer, randomSupplier, cullSides, model, yOffset);
		}
		SnowVariant snowVariant = (SnowVariant) state.getBlock();
		BlockState snow = snowVariant.getSnowState(state, world, pos);
		if (!snow.isAir() && (layer == null || layer == RenderType.solid())) {
			if (snow == Blocks.SNOW.defaultBlockState()) {
				if (cachedSnowModel == null) {
					cachedSnowModel = ClientProxy.getBlockModel(snow);
				}
				model = cachedSnowModel;
			} else {
				model = ClientProxy.getBlockModel(snow);
			}
			double yOffset = CoreModule.SLAB.is(state) ? 0.5 : 0;
			rendered |= api.translateYAndRender(world, snow, pos, layer, randomSupplier, cullSides, model, yOffset);
		}
		if (options.renderOverlay && (layer == null || layer == RenderType.cutoutMipped()) && (!useVariant || CoreModule.TILE_BLOCK.is(state))) {
			BlockPos pos2 = pos;
			double yOffset;
			if (CoreModule.TILE_BLOCK.is(state) || CoreModule.SLAB.is(state)) {
				if (cachedOverlayModel == null) {
					cachedOverlayModel = ClientProxy.getBlockModel(OVERLAY_MODEL);
				}
				model = cachedOverlayModel;
				if (CoreModule.SLAB.is(state)) {
					yOffset = -0.375;
				} else {
					yOffset = -1;
					pos2 = pos.below();
				}
			} else {
				yOffset = (float) snowVariant.getYOffset();
				model = ClientProxy.getBlockModel(state);
			}
			if (snowVariant.layers(state, world, pos) == 8) {
				yOffset -= 0.002;
			}
			rendered |= api.translateYAndRender(world, state, pos2, layer, randomSupplier, cullSides, model, yOffset);
		}
		return rendered;
	}

}
