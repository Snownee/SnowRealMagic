package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.entity.SnowBlockEntity.Options;

public interface WatcherSnowVariant extends SnowVariant {

	@Override
	default double srm$getYOffset() {
		return 0.125;
	}

	default boolean updateOptions(BlockState state, BlockGetter level, BlockPos pos, Options options) {
		boolean ro = level.getBlockState(pos.above()).isAir();
		return options.update(ro);
	}

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
}
