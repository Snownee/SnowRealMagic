package snownee.snow.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import snownee.snow.client.ClientVariables;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

	// we clear the mapping before user refreshing resources
	// we don't do this after BlockModelShaper.rebuildCache
	// because some other mods will manually rebuild the cache
	@Inject(at = @At("HEAD"), method = "reload")
	private void srm_reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2, CallbackInfoReturnable<CompletableFuture<Void>> ci) {
		ClientVariables.snowVariantMapping.clear();
	}
}
