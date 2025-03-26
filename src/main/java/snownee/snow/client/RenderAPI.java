package snownee.snow.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface RenderAPI {

	boolean render(BlockState blockState, BlockPos pos, boolean cullSides, BakedModel model, double yOffset, ModelPart part);

	enum ModelPart {
		CAMO,
		SNOW_LAYER,
		SNOW_OVERLAY,
		DECORATION,
		UNDEFINED
	}

}
