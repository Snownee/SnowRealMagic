package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;
import snownee.snow.block.entity.SnowBlockEntity.Options;

public interface WatcherSnowVariant extends SnowVariant {

	@Override
	default double getYOffset() {
		return 0.125;
	}

	default boolean updateOptions(BlockState state, BlockGetter level, BlockPos pos, Options options) {
		boolean ro = level.getBlockState(pos.above()).isAir();
		boolean rb = Hooks.canSurvive(state, level, pos, true);
		return options.update(ro, rb);
	}

}
