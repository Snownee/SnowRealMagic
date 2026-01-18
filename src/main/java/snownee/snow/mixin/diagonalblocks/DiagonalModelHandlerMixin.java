package snownee.snow.mixin.diagonalblocks;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import fuzs.diagonalblocks.impl.client.handler.DiagonalModelHandler;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import snownee.snow.client.model.SnowCoveredModel;
import snownee.snow.client.model.WrapperUnbakedModel;

@Mixin(value = DiagonalModelHandler.class)
public class DiagonalModelHandlerMixin {
	@SuppressWarnings({"unchecked", "MixinExtrasOperationParameters"})
	@WrapOperation(
			method = "transformBlockModelDefinition",
			at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
	private static <T, R> R srm_unwrap(
			Function<T, R> function,
			T t,
			Operation<R> original,
			@Share("snowCovered") LocalBooleanRef snowCovered) {
		R r = original.call(function, t);
		if (r instanceof WrapperUnbakedModel model) {
			r = (R) model.wrapped();
			snowCovered.set(true);
		}
		return r;
	}

	@Inject(
			method = "transformBlockModelDefinition",
			at = @At(
					value = "FIELD",
					target = "Lfuzs/diagonalblocks/client/handler/DiagonalModelHandler;UNBAKED_MODEL_CACHE:Ljava/util/Map;",
					ordinal = 2))
	private static void srm_wrap(
			ModelIdentifier modelLocation,
			Supplier<UnbakedModel> unbakedModel,
			Function<ModelIdentifier, UnbakedModel> modelGetter,
			BiConsumer<Identifier, UnbakedModel> modelAdder,
			CallbackInfoReturnable<EventResultHolder<UnbakedModel>> cir,
			@Share("snowCovered") LocalBooleanRef snowCovered,
			@Local(ordinal = 1) LocalRef<UnbakedModel> newModel) {
		if (snowCovered.get()) {
			newModel.set(new WrapperUnbakedModel(newModel.get(), SnowCoveredModel::new));
		}
	}
}
