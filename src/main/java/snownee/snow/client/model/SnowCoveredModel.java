package snownee.snow.client.model;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.WatcherSnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.client.ClientVariables;
import snownee.snow.client.SnowClientConfig;

public class SnowCoveredModel extends ForwardingBakedModel {

	public SnowCoveredModel(BakedModel model) {
		wrapped = model;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		Object data = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		if (!(data instanceof SnowBlockEntity))
			return;
		SnowBlockEntity snowBlockEntity = (SnowBlockEntity) data;
		if (state.getBlock() instanceof WatcherSnowVariant) {
			((WatcherSnowVariant) state.getBlock()).updateOptions(state, blockView, pos, snowBlockEntity.options);
		}

		BlockState camo = snowBlockEntity.getState();
		ModelManager modelManager = Minecraft.getInstance().getModelManager();
		BlockModelShaper shaper = modelManager.getBlockModelShaper();
		BakedModel model;

		boolean useSnowVariant = false;
		if (camo.getRenderShape() == RenderShape.MODEL) {
			Vec3 offset = camo.getOffset(blockView, pos);
			BlockColors blockColors = Minecraft.getInstance().getBlockColors();
			context.pushTransform(quad -> {
				int color = -1;
				if (quad.colorIndex() != -1) {
					color = blockColors.getColor(camo, blockView, pos, quad.colorIndex());
					color |= 0xFF000000;
				}
				float offsetY = camo.is(CoreModule.OFFSET_Y) ? 0.101f : 0;
				if (offsetY != 0 || offset != Vec3.ZERO || color != -1) {
					for (int i = 0; i < 4; ++i) {
						quad.pos(i, quad.x(i) + (float) offset.x, quad.y(i) + (float) offset.y + offsetY, quad.z(i) + (float) offset.z);
						quad.spriteColor(i, 0, color);
					}
				}
				return true;
			});
			model = shaper.getBlockModel(camo);
			if (SnowClientConfig.snowVariants && model instanceof SnowVariantModel) {
				BakedModel snowVariant = ((SnowVariantModel) model).getSnowVariant();
				if (snowVariant != null) {
					model = snowVariant;
					useSnowVariant = true;
				}
			}
			((FabricBakedModel) model).emitBlockQuads(blockView, camo, pos, randomSupplier, context);
			context.popTransform();
		}

		if (snowBlockEntity.options.renderBottom) {
			boolean slab = CoreModule.SLAB.is(state);
			int layers = CoreModule.TILE_BLOCK.is(state) ? state.getValue(SnowLayerBlock.LAYERS) : 1;
			context.pushTransform(quad -> {
				if (slab && quad.cullFace() == Direction.DOWN) {
					return false;
				}
				if (quad.cullFace() != Direction.DOWN) { // still can be optimized here
					quad.cullFace(null); // cancel the side cull face
				}
				if (slab) {
					for (int i = 0; i < 4; ++i) {
						quad.pos(i, quad.x(i), quad.y(i) + 0.5F, quad.z(i));
					}
				}
				return true;
			});
			BlockState snow = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
			if (layers == 1) {
				if (ClientVariables.cachedSnowModel == null) {
					ClientVariables.cachedSnowModel = shaper.getBlockModel(snow);
				}
				model = ClientVariables.cachedSnowModel;
			} else {
				model = shaper.getBlockModel(snow);
			}
			((FabricBakedModel) model).emitBlockQuads(blockView, snow, pos, randomSupplier, context);
			context.popTransform();
		}

		//FIXME side cull face not working
		if (snowBlockEntity.options.renderOverlay && (!useSnowVariant || CoreModule.TILE_BLOCK.is(state))) {
			float yOffset = state.getBlock() instanceof SnowVariant ? (float) ((SnowVariant) state.getBlock()).getYOffset() : 0;
			context.pushTransform(quad -> {
				if (yOffset != 0) {
					for (int i = 0; i < 4; ++i) {
						quad.pos(i, quad.x(i), quad.y(i) + yOffset, quad.z(i));
					}
				}
				return true;
			});
			BlockPos pos2 = pos;
			if (CoreModule.TILE_BLOCK.is(state) || CoreModule.SLAB.is(state)) {
				if (ClientVariables.cachedOverlayModel == null) {
					ClientVariables.cachedOverlayModel = BakedModelManagerHelper.getModel(modelManager, ClientVariables.OVERLAY_MODEL);
				}
				model = ClientVariables.cachedOverlayModel;
				if (CoreModule.TILE_BLOCK.is(state)) {
					pos2 = pos2.below();
				}
			} else {
				model = wrapped;
			}
			((FabricBakedModel) model).emitBlockQuads(blockView, state, pos2, randomSupplier, context);
			context.popTransform();
		}
	}

	@Override
	public ItemTransforms getTransforms() {
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

}
