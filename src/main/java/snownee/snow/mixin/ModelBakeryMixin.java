package snownee.snow.mixin;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import snownee.snow.client.ClientVariables;
import snownee.snow.client.SnowVariantMetadataSectionSerializer;
import snownee.snow.client.model.ModelDefinition;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

	@Shadow
	private Set<ResourceLocation> loadingStack;
	@Shadow
	private Map<ResourceLocation, UnbakedModel> topLevelModels;

	private final Set<ResourceLocation> snowModels = Sets.newHashSet();

	@Inject(at = @At("TAIL"), method = "loadBlockModel", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void srm_loadBlockModel(ResourceLocation resourceLocation, CallbackInfoReturnable<BlockModel> ci, ResourceLocation resourceLocation2, BlockModel blockModel) throws IOException {
		if (blockModel == null) {
			return;
		}
		var resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation2);
		if (resource.isEmpty()) {
			return;
		}
		ModelDefinition def = resource.get().metadata().getSection(SnowVariantMetadataSectionSerializer.SERIALIZER).orElse(null);
		if (def != null && def.model != null) {
			loadingStack.add(def.model);
			snowModels.add(def.model);
			ClientVariables.snowVariantMapping.put(resourceLocation, def);
		} else if (snowModels.contains(resourceLocation)) {
			topLevelModels.put(resourceLocation, blockModel);
		}
	}

	@Inject(at = @At("TAIL"), method = "<init>")
	private void srm_clearSnowModels(CallbackInfo ci) {
		snowModels.clear();
	}

}
