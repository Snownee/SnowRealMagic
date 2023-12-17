package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.SnowVariant;

@Mixin(Block.class)
public class BlockMixin {

	@Shadow
	private BlockState defaultBlockState;

	@Inject(at = @At("HEAD"), method = "isExceptionForConnection", cancellable = true)
	private static void srm_isExceptionForConnection(BlockState state, CallbackInfoReturnable<Boolean> ci) {
		if (state.getBlock() instanceof SnowLayerBlock) {
			ci.setReturnValue(true);
		}
	}

	@Inject(method = "registerDefaultState", at = @At("RETURN"))
	private void srm_registerDefaultState(BlockState state, CallbackInfo ci) {
		if (state.hasProperty(SnowVariant.OPTIONAL_LAYERS)) {
			defaultBlockState = state.setValue(SnowVariant.OPTIONAL_LAYERS, 1);
		}
	}

}
