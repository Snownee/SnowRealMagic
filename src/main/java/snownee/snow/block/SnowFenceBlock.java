package snownee.snow.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SoundType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import snownee.snow.CoreModule;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.WrappedSoundType;

public class SnowFenceBlock extends FenceBlock implements WaterLoggableSnowVariant, WatcherSnowVariant {

	public SnowFenceBlock(Properties properties) {
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
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (SnowCommonConfig.retainOriginalBlocks || ModUtil.shouldMelt(worldIn, pos)) {
			worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public SoundType getSoundType(BlockState state) {
		return WrappedSoundType.get(super.getSoundType(state));
	}

	@Override
	public String getTranslationKey() {
		if (this == CoreModule.FENCE) {
			return super.getTranslationKey();
		} else {
			return CoreModule.FENCE.getTranslationKey();
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		updateOptions(worldIn, pos);
	}
}
