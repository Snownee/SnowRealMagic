package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import snownee.snow.world.gen.feature.WorldModule;

@Mixin(DefaultBiomeFeatures.class)
public class MixinDefaultBiomeFeatures {

	@Inject(method = "withFrozenTopLayer", at = @At("HEAD"), cancellable = true)
	private static void srm_addFreezeTopLayer(BiomeGenerationSettings.Builder builder, CallbackInfo info) {
		builder.withFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, WorldModule.CONFIGURED_FEATURE);
		info.cancel();
	}
}
