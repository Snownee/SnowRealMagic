package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowTile.Options;

public interface WatcherSnowVariant extends SnowVariant {

	@Override
	default double getYOffset() {
		return 0.125;
	}

	@SuppressWarnings("deprecation")
	default boolean updateOptions(BlockState state, IBlockReader level, BlockPos pos, Options options) {
		boolean ro = level.getBlockState(pos.up()).isAir();
		boolean rb = CoreModule.BLOCK.isValidPosition(state, level, pos, true);
		return options.update(ro, rb);
	}

}
