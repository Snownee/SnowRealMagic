package snownee.snow.client.model;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import snownee.snow.CoreModule;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.client.SnowClientConfig;

public class SnowVariantModel extends BakedModelWrapper<BakedModel> {

	public static final ModelData USE_SNOW_VARIANT = ModelData.builder().build();
	private final BakedModel variantModel;

	public SnowVariantModel(BakedModel model, BakedModel variantModel) {
		super(model);
		this.variantModel = variantModel;
	}

	@Override
	public List<BakedQuad> getQuads(
			BlockState state,
			Direction side,
			RandomSource rand,
			ModelData extraData,
			@Nullable RenderType renderType) {
		BakedModel model;
		if (extraData == USE_SNOW_VARIANT) {
			model = variantModel;
		} else {
			model = originalModel;
		}

		return model.getQuads(state, side, rand, extraData, renderType);
	}

	@Override
	public ModelData getModelData(
			@NotNull BlockAndTintGetter blockView,
			@NotNull BlockPos pos,
			@NotNull BlockState state,
			@NotNull ModelData modelData) {
		if (SnowClientConfig.snowVariants && modelData != SnowVariantModel.USE_SNOW_VARIANT) {
			if (modelData.has(SnowBlockEntity.OPTIONS)) {
				return USE_SNOW_VARIANT;
			}
			if (state.hasProperty(DoublePlantBlock.HALF) && CoreModule.TILE_BLOCK.is(blockView.getBlockState(pos.below()))) {
				return USE_SNOW_VARIANT;
			}
		}
		return super.getModelData(blockView, pos, state, modelData);
	}
}
