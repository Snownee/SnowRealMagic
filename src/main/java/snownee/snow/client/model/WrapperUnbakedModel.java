package snownee.snow.client.model;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public class WrapperUnbakedModel implements UnbakedModel {
	private final UnbakedModel wrapped;
	private final UnaryOperator<BakedModel> transformer;

	public WrapperUnbakedModel(UnbakedModel wrapped, UnaryOperator<BakedModel> transformer) {
		this.wrapped = wrapped;
		this.transformer = transformer;
	}

	@Override
	public @NotNull Collection<ResourceLocation> getDependencies() {
		return wrapped.getDependencies();
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
		wrapped.resolveParents(function);
	}

	@Nullable
	@Override
	public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation) {
		BakedModel baked = wrapped.bake(modelBaker, function, modelState, resourceLocation);
		return baked == null ? null : transformer.apply(baked);
	}
}
