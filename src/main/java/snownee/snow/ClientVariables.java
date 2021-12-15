package snownee.snow;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.snow.block.entity.SnowBlockEntity.Options;

@OnlyIn(Dist.CLIENT)
public final class ClientVariables {

	public static final Options fallbackOptions = new Options();
	public static BakedModel cachedSnowModel;
	public static BakedModel cachedOverlayModel;

}
