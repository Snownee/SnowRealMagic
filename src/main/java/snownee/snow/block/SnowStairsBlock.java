package snownee.snow.block;

import java.util.Objects;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.properties.Half;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;

import javax.annotation.Nonnull;

public class SnowStairsBlock extends StairsBlock implements WaterLoggableSnowVariant {

	@SuppressWarnings("deprecation")
	public SnowStairsBlock(Properties properties) {
		super(Blocks.STONE.getDefaultState(), properties);
	}

	@Override
	public void onReplaced(BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeTileEntity(pos);
		}
		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public boolean ticksRandomly(@Nonnull BlockState state) {
		return true;
	}

	@Override
	public void randomTick(@Nonnull BlockState state, @Nonnull ServerWorld worldIn, @Nonnull BlockPos pos, @Nonnull Random random) {
		if (SnowCommonConfig.retainOriginalBlocks || ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
		}
	}

	@Override
	public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
		return Objects.requireNonNull(super.getStateForPlacement(context)).with(HALF, Half.BOTTOM);
	}

	@Override
	public float getPlayerRelativeBlockHardness(@Nonnull BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
		return getRaw(state, worldIn, pos).getPlayerRelativeBlockHardness(player, worldIn, pos);
	}

	@Override
	public double getYOffset() {
		return 0.125;
	}
}
