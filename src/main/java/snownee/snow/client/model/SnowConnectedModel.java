package snownee.snow.client.model;

import java.util.List;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.SnowClientConfig;

public class SnowConnectedModel extends BakedModelWrapper<BakedModel> implements SnowVariantModel {

	public static final ModelData USE_SNOW_VARIANT = ModelData.builder().build();

	public SnowConnectedModel(BakedModel model) {
		super(model);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
		BakedModel model = null;
		if (extraData == USE_SNOW_VARIANT) {
			model = getSnowVariant();
		}
		if (model == null) {
			model = originalModel;
		}

		return model.getQuads(state, side, rand, extraData, renderType);
	}

	@Nonnull
	@Override
	public ModelData getModelData(@Nonnull BlockAndTintGetter blockView, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
		if (SnowClientConfig.snowVariants && CoreModule.TILE_BLOCK.is(blockView.getBlockState(pos.below()))) {
			return USE_SNOW_VARIANT;
		}
		return super.getModelData(blockView, pos, state, tileData);
	}

	@Override
	public BakedModel getSnowVariant() {
		if (originalModel instanceof SnowVariantModel) {
			return ((SnowVariantModel) originalModel).getSnowVariant();
		}
		return null;
	}

	@Override
	public void setSnowVariant(BakedModel model) {
		if (originalModel instanceof SnowVariantModel) {
			((SnowVariantModel) originalModel).setSnowVariant(model);
		} else {
			SnowRealMagic.LOGGER.error("Cannot set snow variant model for {}", originalModel);
		}
	}

}
