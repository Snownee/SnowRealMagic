package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import snownee.snow.client.ClientHooks;
import snownee.snow.client.model.ModelMetadataSection;
import snownee.snow.client.model.SnowyBlockModelPart;

@Mixin(Variant.class)
public class VariantMixin {
	@Shadow
	@Final
	private Identifier modelLocation;

	@Inject(method = "resolveDependencies", at = @At("TAIL"))
	private void srm_resolveDependencies(ResolvableModel.Resolver resolver, CallbackInfo ci) {
		ModelMetadataSection section = ClientHooks.snowVariantMapping.get(modelLocation);
		if (section != null) {
			resolver.markDependency(section.model());
		}
	}

	@WrapOperation(
			method = "bake",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/block/model/SimpleModelWrapper;bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/renderer/block/model/BlockModelPart;"))
	private BlockModelPart srm_bake(ModelBaker modelBakery, Identifier location, ModelState state, Operation<BlockModelPart> original) {
		BlockModelPart normalPart = original.call(modelBakery, location, state);
		ModelMetadataSection section = ClientHooks.snowVariantMapping.get(modelLocation);
		if (section == null) {
			return normalPart;
		}
		BlockModelPart snowyPart = original.call(modelBakery, section.model(), state);
		return new SnowyBlockModelPart(normalPart, snowyPart);
	}
}
