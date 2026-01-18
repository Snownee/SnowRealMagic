package snownee.snow.convert;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;

public class CoveredBlockConverter implements BlockConverter {
	protected final Class<? extends Block> clazz;
	protected final BlockState result;

	public CoveredBlockConverter(Class<? extends Block> clazz, BlockState result) {
		this.clazz = clazz;
		this.result = result;
	}

	@Override
	public boolean takeIn(BlockState blockState) {
		return clazz.isInstance(blockState.getBlock());
	}

	@Override
	public boolean accept(BlockState blockState) {
		return Hooks.hasAllProperties(blockState, result(blockState));
	}

	@Override
	public BlockState convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers) {
		return Hooks.copyProperties(blockState, result(blockState));
	}

	public BlockState result(BlockState blockState) {
		return result;
	}

	public Class<? extends Block> clazz() {
		return clazz;
	}
}
