package snownee.snow.client.model;

import java.util.function.UnaryOperator;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;

public class WrapperUnbakedModel implements BlockStateModel.Unbaked {
	private final BlockStateModel.Unbaked wrapped;
	private final UnaryOperator<BlockStateModel> transformer;

	public WrapperUnbakedModel(BlockStateModel.Unbaked wrapped, UnaryOperator<BlockStateModel> transformer) {
		this.wrapped = wrapped;
		this.transformer = transformer;
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		wrapped.resolveDependencies(resolver);
	}

	@Override
	public BlockStateModel bake(ModelBaker modelBakery) {
		return transformer.apply(wrapped.bake(modelBakery));
	}

	public final BlockStateModel.Unbaked wrapped() {
		return wrapped;
	}
}
