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

    @Inject(method = "func_243730_an", at = @At("HEAD"), cancellable = true)
    private static void srm_addFreezeTopLayer(BiomeGenerationSettings.Builder builder, CallbackInfo info) {
        builder.func_242513_a(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, WorldModule.CONFIGURED_FEATURE);
        info.cancel();
    }

    //    public static void func_243730_an(BiomeGenerationSettings.Builder p_243730_0_) {
    //        p_243730_0_.func_242513_a(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, Features.field_243794_T);
    //    }
}
