package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import snownee.snow.client.model.SnowVariantModel;

@Mixin(SimpleBakedModel.class)
public abstract class SimpleBakedModelMixin implements SnowVariantModel {

	@Unique
	private BakedModel srm$snowVariant;

	@Override
	public BakedModel srm$getSnowVariant() {
		return srm$snowVariant;
	}

	@Override
	public void srm$setSnowVariant(BakedModel model) {
		srm$snowVariant = model;
	}

}