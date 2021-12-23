package snownee.snow.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.entity.SnowBlockEntity.Options;

@Environment(EnvType.CLIENT)
public final class ClientVariables {

	public static final Options fallbackOptions = new Options();
	public static BakedModel cachedSnowModel;
	public static BakedModel cachedOverlayModel;

	public static final ResourceLocation OVERLAY_MODEL = new ResourceLocation(SnowRealMagic.MODID, "block/overlay");

}
