package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import snownee.snow.Hook;

@Mixin(Block.class)
public class MixinBlock {

	@Inject(at = @At("HEAD"), method = "shouldSideBeRendered", cancellable = true)
	private static void srm_shouldRenderFace(BlockState state, IBlockReader level, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> ci) {
		if (Hook.shouldRenderFaceSnow(state, level, pos, direction)) {
			ci.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "cannotAttach", cancellable = true)
	private static void srm_cannotAttach(Block blockIn, CallbackInfoReturnable<Boolean> ci) {
		if (blockIn instanceof SnowBlock) {
			ci.setReturnValue(true);
		}
	}

}
