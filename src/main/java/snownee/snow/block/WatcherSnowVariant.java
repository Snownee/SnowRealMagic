package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.entity.SnowBlockEntity.Options;

public interface WatcherSnowVariant extends SnowVariant {

	@Override
	default double getYOffset() {
		return 0.125;
	}

	default boolean updateOptions(BlockState state, BlockGetter level, BlockPos pos, Options options) {
		boolean ro = level.getBlockState(pos.above()).isAir();
		return options.update(ro);
	}

	@Override
	default int layers(BlockState state, BlockGetter world, BlockPos pos) {
		return state.getValue(OPTIONAL_LAYERS);
	}

	@Override
	default BlockState onShovel(BlockState state, Level world, BlockPos pos) {
		int layers = state.getValue(OPTIONAL_LAYERS) - 1;
		if (layers >= 0) {
			return state.setValue(OPTIONAL_LAYERS, layers);
		} else {
			return getRaw(state, world, pos);
		}
	}
}
