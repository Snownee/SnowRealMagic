package snownee.snow.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.block.SpreadableSnowyDirtBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;
import snownee.snow.CoreModule;
import snownee.snow.Hook;

@Mixin(SpreadableSnowyDirtBlock.class)
public abstract class MixinSpreadableSnowyDirtBlock {

	@Inject(at = @At("HEAD"), method = "isSnowyConditions", cancellable = true)
	private static void srm_isSnowyConditions(BlockState state, IWorldReader viewableWorld, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(Hook.canSurvive(state, viewableWorld, blockPos));
	}

	@Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
	public void srm_randomTickProxy(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random, CallbackInfo ci) {
		if (!world.isRemote) {
			if (!Hook.canSurvive(blockState, world, blockPos)) {
				world.setBlockState(blockPos, Blocks.DIRT.getDefaultState());
			} else {
				if (world.getLight(blockPos.up()) >= 9) {
					BlockState blockState2 = ((Block) (Object) this).getDefaultState();

					for (int i = 0; i < 4; ++i) {
						BlockPos blockPos2 = blockPos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
						if (world.getBlockState(blockPos2).getBlock() == Blocks.DIRT && isSnowyAndNotUnderwater(blockState2, world, blockPos2)) {
							Block upBlock = world.getBlockState(blockPos2.up()).getBlock();
							world.setBlockState(blockPos2, blockState2.with(SnowyDirtBlock.SNOWY, upBlock.isIn(CoreModule.BOTTOM_SNOW)));
						}
					}
				}

			}
		}
		ci.cancel();
	}

	@Shadow
	public static boolean isSnowyAndNotUnderwater(BlockState p_220256_0_, IWorldReader p_220256_1_, BlockPos p_220256_2_) {
		return false;
	}

}
