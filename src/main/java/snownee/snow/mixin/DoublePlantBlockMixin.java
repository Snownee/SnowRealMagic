package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import snownee.snow.CoreModule;

@Mixin(DoublePlantBlock.class)
public class DoublePlantBlockMixin {
	@Shadow
	@Final
	public static EnumProperty<DoubleBlockHalf> HALF;

	// TODO 确认逻辑
	@Inject(
			method = "preventDropFromBottomPart",
			at = @At(
					value = "TAIL"))
	private static void srm_preventDropFromBottomPart(
			Level level,
			BlockPos pos,
			BlockState state,
			Player player,
			CallbackInfo ci,
			@Local DoubleBlockHalf doubleBlockHalf) {
		if (doubleBlockHalf != DoubleBlockHalf.UPPER) {
			return;
		}
		var belowPos = pos.below();
		var belowState = level.getBlockState(belowPos);
		if (!CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(belowState)) {
			return;
		}
		level.setBlock(belowPos, Blocks.AIR.defaultBlockState(), 35);
		level.setBlock(
				belowPos,
				Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, belowState.getValue(SnowLayerBlock.LAYERS)),
				35);
		level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, belowPos, Block.getId(state));
	}

	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void srm_updateShape(
			BlockState stateIn,
			Direction facing,
			BlockState facingState,
			LevelAccessor worldIn,
			BlockPos currentPos,
			BlockPos facingPos,
			CallbackInfoReturnable<BlockState> cir) {
		var doubleblockhalf = stateIn.getValue(HALF);
		if (facing.getAxis() == Direction.Axis.Y && CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(facingState)) {
			if ((doubleblockhalf == DoubleBlockHalf.UPPER && facing == Direction.DOWN) ||
					(doubleblockhalf == DoubleBlockHalf.LOWER && facing == Direction.UP)) {
				cir.setReturnValue(stateIn);
			}
		}
	}

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	public void srm_canSurvive(BlockState state, LevelReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER && CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(worldIn.getBlockState(pos.below()))) {
			cir.setReturnValue(true);
		}
	}
}
