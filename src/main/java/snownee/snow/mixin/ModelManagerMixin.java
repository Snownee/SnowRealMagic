package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.snow.client.SnowClient;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

	// we clear the mapping before user refreshing resources
	// we don't do this after BlockModelShaper.rebuildCache
	// because some other mods will manually rebuild the cache
	@Inject(at = @At("HEAD"), method = "prepare")
	private void srm_prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<ModelBakery> ci) {
		SnowClient.snowVariantMapping.clear();
	}
}
