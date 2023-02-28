package snownee.snow.block;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;

import javax.annotation.Nonnull;

public class SnowFenceGateBlock extends FenceGateBlock implements WatcherSnowVariant {

	public SnowFenceGateBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SnowTextureTile();
	}

	@Override
	public void randomTick(@Nonnull BlockState state, @Nonnull ServerWorld worldIn, @Nonnull BlockPos pos, @Nonnull Random random) {
		if (SnowCommonConfig.retainOriginalBlocks || ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
		}
	}

	@Override
	public float getPlayerRelativeBlockHardness(@Nonnull BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
		return getRaw(state, worldIn, pos).getPlayerRelativeBlockHardness(player, worldIn, pos);
	}

}
