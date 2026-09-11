package snownee.snow.mixin.farmersdelight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.SnowFenceGateBlock;
import snownee.snow.compat.farmersdelight.SnowRopeFenceBlock;
import vectorwing.farmersdelight.common.block.RopeFenceBlock;

@Mixin(value = RopeFenceBlock.class, remap = false)
public abstract class RopeFenceBlockMixin {

	@Inject(method = "isSameFence", at = @At("RETURN"), cancellable = true)
	private void srm_isSameFence(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && (state.getBlock() instanceof SnowRopeFenceBlock || state.getBlock() instanceof SnowFenceGateBlock)) {
			cir.setReturnValue(true);
		}
	}
}