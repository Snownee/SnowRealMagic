package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Block.class)
public class BlockMixin {

	@Inject(at = @At("HEAD"), method = "isExceptionForConnection", cancellable = true)
	private static void srm_isExceptionForConnection(BlockState state, CallbackInfoReturnable<Boolean> ci) {
		if (state.getBlock() instanceof SnowLayerBlock) {
			ci.setReturnValue(true);
		}
	}

}
