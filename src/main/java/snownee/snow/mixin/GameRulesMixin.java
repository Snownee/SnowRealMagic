package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import snownee.snow.SnowCommonConfig;

@Mixin(value = GameRules.class, priority = 200)
public class GameRulesMixin {

	/**
	 * @reason Disable the vanilla snow accumulation
	 */
	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private <T> void srm_get(GameRule<T> gameRule, CallbackInfoReturnable<T> ci) {
		if (gameRule == GameRules.MAX_SNOW_ACCUMULATION_HEIGHT && !SnowCommonConfig.forceVanillaIceSnowLogic) {
			//noinspection unchecked
			ci.setReturnValue((T) Integer.valueOf(0));
		}
	}

}
