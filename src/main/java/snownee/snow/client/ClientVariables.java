package snownee.snow.client;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.snow.block.SnowTile.Options;

@OnlyIn(Dist.CLIENT)
public final class ClientVariables {

	public static final Options fallbackOptions = new Options();
	public static IBakedModel cachedSnowModel;
	public static IBakedModel cachedOverlayModel;

}
