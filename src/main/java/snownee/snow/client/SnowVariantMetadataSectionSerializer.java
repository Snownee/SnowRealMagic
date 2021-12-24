package snownee.snow.client;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import snownee.kiwi.util.Util;

public class SnowVariantMetadataSectionSerializer implements MetadataSectionSerializer<ResourceLocation> {

	public static final SnowVariantMetadataSectionSerializer SERIALIZER = new SnowVariantMetadataSectionSerializer();

	@Override
	public String getMetadataSectionName() {
		return "srm";
	}

	@Override
	public ResourceLocation fromJson(JsonObject o) {
		return Util.RL(o.get("model").getAsString());
	}

}
