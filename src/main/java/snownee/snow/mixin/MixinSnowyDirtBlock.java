package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import snownee.snow.CoreModule;

@Mixin(SnowyDirtBlock.class)
public class MixinSnowyDirtBlock extends Block {

	public MixinSnowyDirtBlock(Properties properties) {
		super(properties);
	}

	@Inject(at = @At("HEAD"), method = "updatePostPlacement", cancellable = true)
	public void srm_updatePostPlacementProxy(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir) {
		if (facing != Direction.UP) {
			cir.setReturnValue(stateIn);
		} else {
			Block block = facingState.getBlock();
			cir.setReturnValue(stateIn.with(SnowyDirtBlock.SNOWY, block == Blocks.SNOW_BLOCK || block.isIn(CoreModule.BOTTOM_SNOW)));
		}
	}

	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	public void srm_getStateForPlacementProxy(BlockItemUseContext context, CallbackInfoReturnable<BlockState> cir) {
		Block block = context.getWorld().getBlockState(context.getPos().up()).getBlock();
		cir.setReturnValue(getDefaultState().with(SnowyDirtBlock.SNOWY, block == Blocks.SNOW_BLOCK || block.isIn(CoreModule.BOTTOM_SNOW)));
	}
}
