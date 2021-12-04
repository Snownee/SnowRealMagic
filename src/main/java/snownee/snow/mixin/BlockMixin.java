package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;

@Mixin(Block.class)
public class BlockMixin {

	@Inject(at = @At("HEAD"), method = "shouldRenderFace", cancellable = true)
	private static void srm_shouldRenderFace(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockPos relativePos, CallbackInfoReturnable<Boolean> ci) {
		if (Hooks.shouldRenderFaceSnow(state, level, pos, direction, relativePos)) {
			ci.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "isExceptionForConnection", cancellable = true)
	private static void srm_isExceptionForConnection(BlockState state, CallbackInfoReturnable<Boolean> ci) {
		if (state.getBlock() instanceof SnowLayerBlock) {
			ci.setReturnValue(true);
		}
	}

}
