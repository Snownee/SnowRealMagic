package snownee.snow.mixin;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.snow.client.SnowClient;
import snownee.snow.client.SnowVariantMetadataSectionSerializer;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowVariantModel;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

	@Shadow
	private Set<ResourceLocation> loadingStack;
	@Shadow
	private Map<ResourceLocation, UnbakedModel> topLevelModels;
	@Shadow
	private ResourceManager resourceManager;

	private final Set<ResourceLocation> snowModels = Sets.newHashSet();

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockModel;fromStream(Ljava/io/Reader;)Lnet/minecraft/client/renderer/block/model/BlockModel;", shift = Shift.BY, by = 2
			), method = "loadBlockModel", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void srm_loadBlockModel(ResourceLocation resourceLocation, CallbackInfoReturnable<BlockModel> ci, Reader reader, BlockModel blockModel) throws IOException {
		if (blockModel == null) {
			return;
		}
		var file = new ResourceLocation(resourceLocation.getNamespace(), "models/" + resourceLocation.getPath() + ".json");
		var resource = resourceManager.getResource(file);
		if (resource.isEmpty()) {
			return;
		}
		ModelDefinition def = resource.get().metadata().getSection(SnowVariantMetadataSectionSerializer.SERIALIZER).orElse(null);
		if (def != null && def.model != null) {
			loadingStack.add(def.model);
			snowModels.add(def.model);
			SnowClient.snowVariantMapping.put(resourceLocation, def);
		} else if (snowModels.contains(resourceLocation)) {
			topLevelModels.put(resourceLocation, blockModel);
		}
	}

	@SuppressWarnings("rawtypes")
	@Inject(
			at = @At(
				"TAIL"
			), method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;Ljava/util/function/Function;)Lnet/minecraft/client/resources/model/BakedModel;", locals = LocalCapture.CAPTURE_FAILHARD, remap = false
	)
	private void srm_bake(ResourceLocation resourceLocation, ModelState modelState, Function<Material, TextureAtlasSprite> sprites, CallbackInfoReturnable<BakedModel> ci, Triple triple, UnbakedModel unbakedModel, BakedModel blockModel) {
		if (!(blockModel instanceof SnowVariantModel) || modelState.getClass() != Variant.class) {
			return;
		}
		ModelDefinition def = SnowClient.snowVariantMapping.get(resourceLocation);
		if (def != null) {
			Variant variantState = (Variant) modelState;
			variantState = new Variant(def.model, variantState.getRotation(), variantState.isUvLocked(), variantState.getWeight());
			BakedModel model = ((ModelBakery) (Object) this).bake(def.model, variantState, sprites);
			((SnowVariantModel) blockModel).setSnowVariant(model);
		}
	}

	@Inject(at = @At("TAIL"), method = "uploadTextures")
	private void srm_uploadTextures(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> ci) {
		snowModels.clear();
	}

}