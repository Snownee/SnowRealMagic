package snownee.snow.mixin.client;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelBakery.ModelBakerImpl;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import snownee.snow.client.SnowClient;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowVariantModel;

@Mixin(value = ModelBakerImpl.class, priority = 800)
public class ModelBakerImplMixin {

	@Shadow(remap = false, aliases = "f_243927_")
	private ModelBakery this$0;

	@Inject(
			at = @At(
				"TAIL"
			), method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;Ljava/util/function/Function;)Lnet/minecraft/client/resources/model/BakedModel;", locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = false, cancellable = true
	)
	private void srm_bake(ResourceLocation resourceLocation, ModelState modelState, Function<Material, TextureAtlasSprite> sprites, CallbackInfoReturnable<BakedModel> ci, ModelBakery.BakedCacheKey key, BakedModel bakedmodel, UnbakedModel unbakedmodel, BakedModel blockModel) {
		if (modelState.getClass() != Variant.class) {
			return;
		}
		ModelDefinition def = SnowClient.snowVariantMapping.get(resourceLocation);
		if (def != null) {
			Variant variantState = (Variant) modelState;
			variantState = new Variant(def.model, variantState.getRotation(), variantState.isUvLocked(), variantState.getWeight());
			BakedModel variantModel = ((ModelBakerImpl) (Object) this).bake(def.model, variantState, sprites);
			BakedModel model = new SnowVariantModel(ci.getReturnValue(), variantModel);
			this$0.bakedCache.put(key, model);
			ci.setReturnValue(model);
		}
	}

}
