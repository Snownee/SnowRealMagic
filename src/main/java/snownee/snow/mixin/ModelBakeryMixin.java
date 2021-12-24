package snownee.snow.mixin;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.snow.Hooks;
import snownee.snow.client.SnowVariantMetadataSectionSerializer;
import snownee.snow.client.model.SnowVariantModel;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

	@Shadow
	private Set<ResourceLocation> loadingStack;
	@Shadow
	private Map<ResourceLocation, UnbakedModel> topLevelModels;

	private final Map<ResourceLocation, ResourceLocation> snowModelIdMapping = Maps.newHashMap();

	@Inject(
			at = @At(
					value = "INVOKE", target = "fromStream", shift = Shift.BY, by = 2
			), method = "loadBlockModel", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void srm_loadBlockModel(ResourceLocation resourceLocation, CallbackInfoReturnable<BlockModel> ci, Reader reader, Resource resource, BlockModel string2) throws IOException {
		if (resource == null || string2 == null) {
			return;
		}
		if (resource.hasMetadata()) {
			ResourceLocation variant = resource.getMetadata(SnowVariantMetadataSectionSerializer.SERIALIZER);
			if (variant != null) {
				loadingStack.add(variant);
				snowModelIdMapping.put(resourceLocation, variant);
			}
		} else if (snowModelIdMapping.containsValue(resourceLocation)) {
			topLevelModels.put(resourceLocation, string2);
		}
	}

	@Inject(at = @At("TAIL"), method = "bake", locals = LocalCapture.CAPTURE_FAILHARD)
	private void srm_bake(ResourceLocation resourceLocation, ModelState modelState, CallbackInfoReturnable<BakedModel> ci, Triple triple, UnbakedModel unbakedModel, BakedModel blockModel) {
		if (!(blockModel instanceof SnowVariantModel) || modelState.getClass() != Variant.class) {
			return;
		}
		ResourceLocation variant = snowModelIdMapping.get(resourceLocation);
		if (variant != null) {
			Hooks.print(variant);
			Variant variantState = (Variant) modelState;
			variantState = new Variant(variant, variantState.getRotation(), variantState.isUvLocked(), variantState.getWeight());
			Hooks.print(((ModelBakery) (Object) this).getModel(variant));
			BakedModel model = ((ModelBakery) (Object) this).bake(variant, variantState);
			Hooks.print(model);
			((SnowVariantModel) blockModel).setSnowVariant(model);
		}
	}

	@Inject(at = @At("TAIL"), method = "uploadTextures")
	private void srm_uploadTextures(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> ci) {
		snowModelIdMapping.clear();
	}

}
