package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.entity.SnowCoveredBlockEntity;

public class SnowFenceGateBlock extends FenceGateBlock implements EntityBlock, WatcherSnowVariant {

	public SnowFenceGateBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SnowCoveredBlockEntity(pos, state);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (SnowCommonConfig.retainOriginalBlocks || ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockAndUpdate(pos, getRaw(state, worldIn, pos));
			worldIn.updateNeighborsAt(pos, state.getBlock());
		}
	}

	//	@Override
	//	public float getPlayerRelativeBlockHardness(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
	//		return getRaw(state, worldIn, pos).getPlayerRelativeBlockHardness(player, worldIn, pos);
	//	}

}
