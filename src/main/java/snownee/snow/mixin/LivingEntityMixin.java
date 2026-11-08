package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
	private void srm_snowStepHeight(CallbackInfoReturnable<Float> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (SnowCommonConfig.snowStepUpMultiplier <= 0 || SnowCommonConfig.snowStepUpMaxIncrease <= 0 ||
				!(entity instanceof Player || entity.getControllingPassenger() instanceof Player) || !entity.onGround()) {
			return;
		}
		BlockState state = entity.getBlockStateOn();
		if (!(state.getBlock() instanceof SnowLayerBlock)) {
			if (!(state.getBlock() instanceof SnowVariant) ||
					state.hasProperty(SnowVariant.OPTIONAL_LAYERS) && state.getValue(SnowVariant.OPTIONAL_LAYERS) == 0) {
				return;
			}
		}
		float stepHeight = cir.getReturnValueF();
		float increase = stepHeight * SnowCommonConfig.snowStepUpMultiplier;
		cir.setReturnValue(stepHeight + Math.min(increase, SnowCommonConfig.snowStepUpMaxIncrease));
	}

}