package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import snownee.snow.client.model.SnowVariantModel;

@Mixin(SimpleBakedModel.class)
public abstract class SimpleBakedModelMixin implements SnowVariantModel {

	private BakedModel snowVariant;

	@Override
	public BakedModel getSnowVariant() {
		return snowVariant;
	}

	@Override
	public void setSnowVariant(BakedModel model) {
		snowVariant = model;
	}

}
