package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;

public class SnowFenceBlock extends FenceBlock implements WaterLoggableSnowVariant, WatcherSnowVariant {

	public SnowFenceBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (SnowCommonConfig.retainOriginalBlocks || ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockAndUpdate(pos, getRaw(state, worldIn, pos));
			worldIn.updateNeighborsAt(pos, state.getBlock());
		}
	}

	@Override
	public String getDescriptionId() {
		if (CoreModule.FENCE.is(this)) {
			return super.getDescriptionId();
		} else {
			return CoreModule.FENCE.get().getDescriptionId();
		}
	}

}
