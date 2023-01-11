package snownee.snow.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.entity.SnowCoveredBlockEntity;
import snownee.snow.mixin.FenceGateBlockAccess;

public class SnowFenceGateBlock extends FenceGateBlock implements EntityBlock, WatcherSnowVariant {

	public SnowFenceGateBlock(Properties properties) {
		super(properties, SoundEvents.FENCE_GATE_CLOSE, SoundEvents.FENCE_GATE_OPEN);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SnowCoveredBlockEntity(pos, state);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (SnowCommonConfig.retainOriginalBlocks || ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockAndUpdate(pos, getRaw(state, worldIn, pos));
		}
	}

	@Override
	public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		adjustSounds(blockState, level, blockPos);
		return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide) {
			adjustSounds(blockState, level, blockPos);
		}
		super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
	}

	private void adjustSounds(BlockState blockState, Level level, BlockPos blockPos) {
		BlockState raw = getRaw(blockState, level, blockPos);
		if (raw.getBlock() instanceof FenceGateBlock) {
			FenceGateBlockAccess rawFenceGate = (FenceGateBlockAccess) raw.getBlock();
			FenceGateBlockAccess fenceGate = (FenceGateBlockAccess) blockState.getBlock();
			fenceGate.setOpenSound(rawFenceGate.getOpenSound());
			fenceGate.setCloseSound(rawFenceGate.getCloseSound());
		}
	}

	//	@Override
	//	public float getPlayerRelativeBlockHardness(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
	//		return getRaw(state, worldIn, pos).getPlayerRelativeBlockHardness(player, worldIn, pos);
	//	}

}
