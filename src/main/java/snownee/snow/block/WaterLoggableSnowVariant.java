package snownee.snow.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import snownee.snow.block.entity.SnowCoveredBlockEntity;

public interface WaterLoggableSnowVariant extends EntityBlock, SnowVariant, SimpleWaterloggedBlock {
	@Override
	default boolean canPlaceLiquid(
			@Nullable Player player,
			BlockGetter blockGetter,
			BlockPos blockPos,
			BlockState blockState,
			Fluid fluid) {
		return fluid.isSame(Fluids.WATER);
	}

	@Override
	default boolean placeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
		BlockState raw = srm$getRaw(state, worldIn, pos);
		if (raw.hasProperty(BlockStateProperties.WATERLOGGED) && fluidStateIn.is(Fluids.WATER)) {
			if (!worldIn.isClientSide()) {
				worldIn.setBlock(pos, raw.setValue(BlockStateProperties.WATERLOGGED, true), 3);
				worldIn.scheduleTick(pos, fluidStateIn.getType(), fluidStateIn.getType().getTickDelay(worldIn));
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	default @NotNull ItemStack pickupBlock(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}

	@Override
	default BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
		return new SnowCoveredBlockEntity(pos, blockState);
	}

}
