package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;

@Mixin(SnowyDirtBlock.class)
public class SnowyDirtBlockMixin extends Block {

	public SnowyDirtBlockMixin(Properties properties) {
		super(properties);
	}

	@Inject(at = @At("HEAD"), method = "updateShape", cancellable = true)
	public void srm_updateShapeProxy(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir) {
		if (facing != Direction.UP) {
			cir.setReturnValue(stateIn);
		} else {
			Block block = facingState.getBlock();
			cir.setReturnValue(stateIn.setValue(SnowyDirtBlock.SNOWY, block == Blocks.SNOW_BLOCK || CoreModule.BOTTOM_SNOW.contains(block)));
		}
	}

	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	public void srm_getStateForPlacementProxy(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
		Block block = context.getLevel().getBlockState(context.getClickedPos().above()).getBlock();
		cir.setReturnValue(defaultBlockState().setValue(SnowyDirtBlock.SNOWY, block == Blocks.SNOW_BLOCK || CoreModule.BOTTOM_SNOW.contains(block)));
	}
}
