package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.block.WaterLoggableSnowVariant;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateMixin {
	@Shadow
	protected abstract BlockState asState();

	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	private void srm_getDestroySpeed(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		BlockState blockState = asState();
		if (blockState.getBlock() instanceof WaterLoggableSnowVariant snowVariant) {
			cir.setReturnValue(snowVariant.getRaw(blockState, level, pos).getDestroySpeed(level, pos));
		}
	}
}
