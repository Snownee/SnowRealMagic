package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery.ModelBakerImpl;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import snownee.snow.client.ClientVariables;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowVariantModel;

@Mixin(value = ModelBakerImpl.class, priority = 1001)
public class ModelBakerImplMixinNormalBake {

	@Inject(
			at = @At(
				"TAIL"
			), method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/BakedModel;", locals = LocalCapture.CAPTURE_FAILEXCEPTION

	)
	private void srm_bake(ResourceLocation resourceLocation, ModelState modelState, CallbackInfoReturnable<BakedModel> ci, @Coerce Object key, UnbakedModel unbakedModel, BakedModel blockModel) {
		if (!(blockModel instanceof SnowVariantModel) || modelState.getClass() != Variant.class) {
			return;
		}
		ModelDefinition def = ClientVariables.snowVariantMapping.get(resourceLocation);
		if (def != null) {
			Variant variantState = (Variant) modelState;
			variantState = new Variant(def.model, variantState.getRotation(), variantState.isUvLocked(), variantState.getWeight());
			BakedModel model = ((ModelBakerImpl) (Object) this).bake(def.model, variantState);
			((SnowVariantModel) blockModel).setSnowVariant(model);
		}
	}

}
