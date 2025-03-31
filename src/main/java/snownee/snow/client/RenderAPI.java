package snownee.snow.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface RenderAPI {

	boolean render(BlockState blockState, BakedModel model, double yOffset, ModelPart part);

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
