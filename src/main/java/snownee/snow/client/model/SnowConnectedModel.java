package snownee.snow.client.model;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.SnowClientConfig;

public class SnowConnectedModel extends BakedModelWrapper<BakedModel> implements SnowVariantModel {

	public static final IModelData USE_SNOW_VARIANT = new ModelDataMap.Builder().build();

	public SnowConnectedModel(BakedModel model) {
		super(model);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		BakedModel model = null;
		if (extraData == USE_SNOW_VARIANT) {
			model = getSnowVariant();
		}
		if (model == null) {
			model = originalModel;
		}

		return model.getQuads(state, side, rand, extraData);
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull BlockAndTintGetter blockView, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
		if (SnowClientConfig.snowVariants && blockView.getBlockState(pos.below()).is(CoreModule.TILE_BLOCK)) {
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
