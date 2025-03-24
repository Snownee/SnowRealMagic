package snownee.snow.convert;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.Hooks;
import snownee.snow.block.SnowVariant;

@NotNullByDefault
public class CoveredBlockConverter implements BlockConverter {
	protected final Class<? extends Block> clazz;
	protected final BlockState result;

	public CoveredBlockConverter(Class<? extends Block> clazz, BlockState result) {
		this.clazz = clazz;
		this.result = result;
	}

	@Override
	public boolean takeIn(BlockState blockState) {
		return clazz.isAssignableFrom(blockState.getBlock().getClass());
	}

	@Override
	public boolean accept(BlockState blockState) {
		return Hooks.hasAllProperties(blockState, result(blockState));
	}

	@Override
	public BlockState convert(LevelAccessor level, BlockPos pos, BlockState blockState, int layers) {
		BlockState newState = Hooks.copyProperties(blockState, result(blockState));
		if (newState.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
			newState = newState.setValue(SnowVariant.OPTIONAL_LAYERS, layers);
			BlockPos posDown = pos.below();
			BlockState stateDown = level.getBlockState(posDown);
			newState = newState.updateShape(Direction.DOWN, stateDown, level, pos, posDown);
		}
		return newState;
	}

	public BlockState result(BlockState blockState) {
		return result;
	}

	public Class<? extends Block> clazz() {
		return clazz;
	}
}
