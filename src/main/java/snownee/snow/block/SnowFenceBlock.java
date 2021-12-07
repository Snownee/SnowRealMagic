package snownee.snow.block;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import snownee.snow.CoreModule;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;

public class SnowFenceBlock extends FenceBlock implements WaterLoggableSnowVariant, WatcherSnowVariant {

	public SnowFenceBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (SnowCommonConfig.retainOriginalBlocks || ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
		}
	}

	@Override
	public String getTranslationKey() {
		if (this == CoreModule.FENCE) {
			return super.getTranslationKey();
		} else {
			return CoreModule.FENCE.getTranslationKey();
		}
	}

}
