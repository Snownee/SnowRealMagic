package snownee.snow.client.model;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.block.entity.RenderData;
import snownee.snow.client.SnowClientConfig;

public class SnowVariantModel extends WrapperBlockStateModel implements SRMModel {

	public SnowVariantModel(BlockStateModel wrapped) {
		super(wrapped);
	}

	@Override
	public boolean isSnowy(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		if (!SnowClientConfig.snowVariants) {
			return false;
		}
		if (((FabricBlockGetter) level).getBlockEntityRenderData(pos) instanceof RenderData) {
			return true;
		}
		return state.hasProperty(DoublePlantBlock.HALF) && CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(level.getBlockState(pos.below()));
	}
}
