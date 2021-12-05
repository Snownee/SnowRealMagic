package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import snownee.snow.CoreModule;
import snownee.snow.block.EntitySnowLayerBlock;

@Mixin(DoublePlantBlock.class)
public class DoublePlantBlockMixin {
	@Shadow
	@Final
	public static EnumProperty<DoubleBlockHalf> HALF;

	@Inject(method = "preventCreativeDropFromBottomPart", at = @At("TAIL"))
	private static void srm_preventCreativeDropFromBottomPart(Level world, BlockPos pos, BlockState state, Player player, CallbackInfo ci) {
		DoubleBlockHalf doubleblockhalf = state.getValue(HALF);
		if (doubleblockhalf == DoubleBlockHalf.UPPER) {
			BlockPos blockpos = pos.below();
			BlockState blockstate = world.getBlockState(blockpos);
			if (blockstate.getBlock() instanceof EntitySnowLayerBlock) {
				world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
				world.setBlock(blockpos, CoreModule.BLOCK.defaultBlockState().setValue(SnowLayerBlock.LAYERS, blockstate.getValue(SnowLayerBlock.LAYERS)), 35);
				world.levelEvent(player, 2001, blockpos, Block.getId(state));
			}
		}
	}

	@Inject(method = "playerWillDestroy", at = @At("TAIL"))
	private void srm_playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player, CallbackInfo ci) {
		srm_preventCreativeDropFromBottomPart(worldIn, pos, state, player, ci);
	}

	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void srm_updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir) {
		DoubleBlockHalf doubleblockhalf = stateIn.getValue(HALF);
		if (facing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.UPPER && facing == Direction.DOWN && facingState.getBlock() instanceof EntitySnowLayerBlock) {
			cir.setReturnValue(stateIn);
		}
		if (facing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.LOWER && facing == Direction.UP && facingState.getBlock() instanceof EntitySnowLayerBlock) {
			cir.setReturnValue(stateIn);
		}
	}

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	public void srm_canSurvive(BlockState state, LevelReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER && worldIn.getBlockState(pos.below()).getBlock() instanceof EntitySnowLayerBlock) {
			cir.setReturnValue(true);
		}
	}
}
