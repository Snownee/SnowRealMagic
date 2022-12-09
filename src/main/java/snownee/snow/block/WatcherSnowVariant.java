package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.block.entity.SnowBlockEntity.Options;

public interface WatcherSnowVariant extends SnowVariant {

	@Override
	default double getYOffset() {
		return 0.125;
	}

	default boolean updateOptions(BlockState state, BlockGetter level, BlockPos pos, Options options) {
		boolean ro = level.getBlockState(pos.above()).isAir();
		boolean rb = CoreModule.BLOCK.get().canSurviveNew(state, level, pos);
		return options.update(ro, rb);
	}

}
