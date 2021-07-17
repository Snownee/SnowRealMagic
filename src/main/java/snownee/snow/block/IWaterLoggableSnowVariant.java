package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public interface IWaterLoggableSnowVariant extends ISnowVariant, IWaterLoggable {
	@Override
	default boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
		return fluidIn == Fluids.WATER;
	}

	@Override
	default boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
		BlockState raw = getRaw(state, worldIn, pos);
		if (raw.hasProperty(BlockStateProperties.WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
			if (!worldIn.isRemote()) {
				worldIn.setBlockState(pos, raw.with(BlockStateProperties.WATERLOGGED, true), 3);
				worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	default Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state) {
		return Fluids.EMPTY;
	}
}
