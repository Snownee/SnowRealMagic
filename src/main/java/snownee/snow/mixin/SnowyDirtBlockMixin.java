package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.CoreModule;

@Mixin(SnowyDirtBlock.class)
public class SnowyDirtBlockMixin {

	@Inject(at = @At("HEAD"), method = "isSnowySetting", cancellable = true)
	private static void srm_getStateForPlacementProxy(BlockState state, CallbackInfoReturnable<Boolean> ci) {
		ci.setReturnValue(state.is(CoreModule.SNOWY_SETTING));
	}

}
