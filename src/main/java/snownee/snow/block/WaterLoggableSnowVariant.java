package snownee.snow.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nonnull;

public interface WaterLoggableSnowVariant extends SnowVariant, IWaterLoggable {
	@Override
	default boolean canContainFluid(@Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluidIn) {
		return fluidIn == Fluids.WATER;
	}

	@Override
	default boolean receiveFluid(@Nonnull IWorld worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull FluidState fluidStateIn) {
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

	@Nonnull
    @Override
	default Fluid pickupFluid(@Nonnull IWorld worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		return Fluids.EMPTY;
	}

	@Override
	default boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	default TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SnowTextureTile();
	}
}
