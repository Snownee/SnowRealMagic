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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import snownee.snow.CoreModule;

@Mixin(DoublePlantBlock.class)
public class DoublePlantBlockMixin {
	@Shadow
	@Final
	public static EnumProperty<DoubleBlockHalf> HALF;

	@Inject(method = "preventDropFromBottomPart", at = @At(value = "TAIL"))
	private static void srm_preventDropFromBottomPart(
			Level level,
			BlockPos pos,
			BlockState state,
			Player player,
			CallbackInfo ci,
			@Local(name = "part") DoubleBlockHalf part) {
		if (part != DoubleBlockHalf.UPPER) {
			return;
		}
		var belowPos = pos.below();
		var belowState = level.getBlockState(belowPos);
		if (!CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(belowState)) {
			return;
		}
		level.setBlock(belowPos, Blocks.AIR.defaultBlockState(), 35);
		level.setBlock(belowPos, CoreModule.SNOWY_DOUBLE_PLANT_LOWER.get().srm$getSnowState(belowState, level, belowPos), 35);
		level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, belowPos, Block.getId(state));
	}

	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void srm_updateShape(
			BlockState state,
			LevelReader level,
			ScheduledTickAccess ticks,
			BlockPos pos,
			Direction directionToNeighbour,
			BlockPos neighbourPos,
			BlockState neighbourState,
			RandomSource random,
			CallbackInfoReturnable<BlockState> cir) {
		// at this time, the raw neighbourState may not be set yet
		DoubleBlockHalf doubleblockhalf = state.getValue(HALF);
		if (doubleblockhalf == DoubleBlockHalf.UPPER && directionToNeighbour == Direction.DOWN && CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(
				neighbourState)) {
			cir.setReturnValue(state);
		}
		if (doubleblockhalf == DoubleBlockHalf.LOWER && directionToNeighbour == Direction.UP && CoreModule.SNOWY_DOUBLE_PLANT_UPPER.is(
				neighbourState)) {
			cir.setReturnValue(state);
		}
	}

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	public void srm_canSurvive(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER && CoreModule.SNOWY_DOUBLE_PLANT_LOWER.is(level.getBlockState(pos.below()))) {
			cir.setReturnValue(true);
		}
	}
}
