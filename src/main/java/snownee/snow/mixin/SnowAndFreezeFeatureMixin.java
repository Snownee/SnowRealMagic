package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import snownee.snow.Hooks;

@Mixin(SnowAndFreezeFeature.class)
public class SnowAndFreezeFeatureMixin {

	@Inject(at = @At("HEAD"), method = "place", cancellable = true)
	private void place(FeaturePlaceContext<NoneFeatureConfiguration> ctx, CallbackInfoReturnable<Boolean> ci) {
		Hooks.placeFeature(ctx);
		ci.setReturnValue(true);
	}

}
