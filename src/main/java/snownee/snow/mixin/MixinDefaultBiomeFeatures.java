package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.IFeatureConfig;
import snownee.snow.MainModule;

@Mixin(DefaultBiomeFeatures.class)
public class MixinDefaultBiomeFeatures {

    @Inject(method = "addFreezeTopLayer", at = @At("HEAD"), cancellable = true)
    private static void srm_addFreezeTopLayer(Biome biomeIn, CallbackInfo info) {
        biomeIn.addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, MainModule.FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
        info.cancel();
    }

}
