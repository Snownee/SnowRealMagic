package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;
import snownee.snow.Hooks;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadableSnowyDirtBlockMixin {

	@Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
	private void srm_randomTickProxy(BlockState blockState, ServerLevel level, BlockPos blockPos, RandomSource random, CallbackInfo ci) {
		if (!canBeGrass(blockState, level, blockPos)) {
			level.setBlockAndUpdate(blockPos, Blocks.DIRT.defaultBlockState());
		} else {
			if (level.getMaxLocalRawBrightness(blockPos.above()) >= 9) {
				BlockState blockstate = ((Block) (Object) this).defaultBlockState();

				for (int i = 0; i < 4; ++i) {
					BlockPos blockPos2 = blockPos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
					if (level.getBlockState(blockPos2).is(Blocks.DIRT) && canPropagate(blockstate, level, blockPos2)) {
						level.setBlockAndUpdate(blockPos2, blockstate.setValue(SnowyDirtBlock.SNOWY, level.getBlockState(blockPos2.above()).is(CoreModule.SNOWY_SETTING)));
					}
				}
			}

		}
		ci.cancel();
	}

	@Shadow
	public static boolean canBeGrass(BlockState p_220256_0_, LevelReader p_220256_1_, BlockPos p_220256_2_) {
		throw new IllegalStateException();
	}

	@Inject(at = @At("HEAD"), method = "canBeGrass", cancellable = true)
	private static void srm_canBeGrass(BlockState state, LevelReader viewableWorld, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(Hooks.canGrassSurvive(state, viewableWorld, blockPos));
	}

	@Shadow
	public static boolean canPropagate(BlockState p_56828_, LevelReader p_56829_, BlockPos p_56830_) {
		throw new IllegalStateException();
	}
}
