package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public interface OptionalLayerSnowVariant extends SnowVariant {

	@Override
	default int srm$layers(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(OPTIONAL_LAYERS);
	}

	@Override
	default int srm$maxLayers(BlockState state, Level level, BlockPos pos2) {
		return 8;
	}

	@Override
	default BlockState srm$decreaseLayer(BlockState state, Level level, BlockPos pos, boolean byPlayer) {
		int layers = state.getValue(OPTIONAL_LAYERS) - 1;
		int minLayers = byPlayer ? 0 : 1;
		if (layers >= minLayers) {
			return state.setValue(OPTIONAL_LAYERS, layers);
		} else {
			return srm$getRaw(state, level, pos);
		}
	}

	@Override
	default boolean srm$canRenderDecoration(BlockState blockState) {
		return true;
	}

	@Override
	default boolean srm$canRenderOverlay(BlockState blockState) {
		return blockState.getValue(OPTIONAL_LAYERS) != 0;
	}
}
