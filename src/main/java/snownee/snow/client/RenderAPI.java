package snownee.snow.client;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface RenderAPI {

	boolean render(BlockState blockState, BlockStateModel model, double yOffset, ModelPart part);

	BlockAndTintGetter level();

	BlockPos pos();

	enum ModelPart {
		CAMO,
		SNOW_LAYER,
		SNOW_OVERLAY,
		DECORATION,
		UNDEFINED
	}

}
