package snownee.snow.mixin;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import snownee.snow.client.ClientVariables;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowVariantModel;

@Mixin(value = ModelBakery.class, priority = 1001)
public class ModelBakeryMixinOptifineBake {

	@Inject(
			at = @At(
				"TAIL"
			), method = "bake(Lnet/minecraft/class_2960;Lnet/minecraft/class_3665;Ljava/util/function/Function;)Lnet/minecraft/class_1087;", locals = LocalCapture.CAPTURE_FAILSOFT, remap = false
	)
	private void srm_bake(ResourceLocation resourceLocation, ModelState modelState, Function<Material, TextureAtlasSprite> sprites, CallbackInfoReturnable<BakedModel> ci, Triple triple, UnbakedModel unbakedModel, BakedModel blockModel) {
		if (!(blockModel instanceof SnowVariantModel) || modelState.getClass() != Variant.class) {
			return;
		}
		ModelDefinition def = ClientVariables.snowVariantMapping.get(resourceLocation);
		if (def != null) {
			Variant variantState = (Variant) modelState;
			variantState = new Variant(def.model, variantState.getRotation(), variantState.isUvLocked(), variantState.getWeight());
			BakedModel model = ((ModelBakery) (Object) this).bake(def.model, variantState);
			((SnowVariantModel) blockModel).setSnowVariant(model);
		}
	}

}
