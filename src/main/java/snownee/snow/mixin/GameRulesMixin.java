package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.GameRules;
import snownee.snow.SnowCommonConfig;

@Mixin(value = GameRules.class, priority = 200)
public class GameRulesMixin {

	/**
	 * @reason Disable the vanilla snow accumulation
	 */
	@Inject(method = "getInt", at = @At("HEAD"), cancellable = true)
	private void srm_getInt(GameRules.Key<GameRules.IntegerValue> key, CallbackInfoReturnable<Integer> ci) {
		if (key == GameRules.RULE_SNOW_ACCUMULATION_HEIGHT && !SnowCommonConfig.forceVanillaIceSnowLogic) {
			ci.setReturnValue(0);
		}
	}

}
