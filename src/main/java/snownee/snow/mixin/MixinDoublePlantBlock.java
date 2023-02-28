package snownee.snow.mixin;

import static net.minecraft.block.SnowBlock.LAYERS;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import snownee.snow.CoreModule;
import snownee.snow.block.ModSnowTileBlock;

@Mixin(DoublePlantBlock.class)
public class MixinDoublePlantBlock {
	@Shadow
	@Final
	public static EnumProperty<DoubleBlockHalf> HALF;

	@Inject(method = "removeBottomHalf", at = @At("TAIL"))
	private static void srm_deleteBottomHalfDoublePlant(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
		DoubleBlockHalf doubleblockhalf = state.get(HALF);
		if (doubleblockhalf == DoubleBlockHalf.UPPER) {
			BlockPos blockpos = pos.down();
			BlockState blockstate = world.getBlockState(blockpos);
			if (blockstate.getBlock() instanceof ModSnowTileBlock) {
				world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 35);
				world.setBlockState(blockpos, CoreModule.BLOCK.getDefaultState().with(LAYERS, blockstate.get(LAYERS)), 35);
				world.playEvent(player, 2001, blockpos, Block.getStateId(state));
			}
		}
	}

	@Inject(method = "onBlockHarvested", at = @At("TAIL"))
	public void srm_onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
		srm_deleteBottomHalfDoublePlant(worldIn, pos, state, player, ci);
	}

	@Inject(method = "updatePostPlacement", at = @At("HEAD"), cancellable = true)
	public void srm_updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir) {
		DoubleBlockHalf doubleblockhalf = stateIn.get(HALF);
		if (facing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.UPPER && facing == Direction.DOWN && facingState.getBlock() instanceof ModSnowTileBlock) {
			cir.setReturnValue(stateIn);
		}
		if (facing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.LOWER && facing == Direction.UP && facingState.getBlock() instanceof ModSnowTileBlock) {
			cir.setReturnValue(stateIn);
		}
	}

	@Inject(method = "isValidPosition", at = @At("HEAD"), cancellable = true)
	public void srm_isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (state.get(HALF) == DoubleBlockHalf.UPPER && worldIn.getBlockState(pos.down()).getBlock() instanceof ModSnowTileBlock) {
			cir.setReturnValue(true);
		}
	}
}
