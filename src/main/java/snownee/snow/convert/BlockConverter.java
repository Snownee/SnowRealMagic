package snownee.snow.convert;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockConverter {
	boolean takeIn(BlockState blockState);

	default boolean accept(BlockState blockState) {
		return true;
	}

	BlockState convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers);

	default boolean acceptAir() {
		return false;
	}
}
