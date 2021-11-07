package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowTile.Options;

public interface WatcherSnowVariant extends SnowVariant {

	@Override
	default double getYOffset() {
		return 0.125;
	}

	default void updateOptions(IBlockReader level, BlockPos pos) {
		TileEntity blockEntity = level.getTileEntity(pos);
		if (blockEntity instanceof SnowTextureTile) {
			((SnowTextureTile) blockEntity).updateOptions();
		}
	}

	@SuppressWarnings("deprecation")
	default boolean onUpdateOptions(BlockState state, IBlockReader level, BlockPos pos, Options options) {
		boolean ro = level.getBlockState(pos.up()).isAir();
		boolean rb = CoreModule.BLOCK.isValidPosition(state, level, pos, true);
		return options.update(ro, rb);
	}

}
