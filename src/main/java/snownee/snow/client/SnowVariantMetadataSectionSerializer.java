package snownee.snow.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import snownee.snow.client.model.ModelDefinition;

public class SnowVariantMetadataSectionSerializer implements MetadataSectionSerializer<ModelDefinition> {

	public static final SnowVariantMetadataSectionSerializer SERIALIZER = new SnowVariantMetadataSectionSerializer();
	private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
			.create();

	@Override
	public String getMetadataSectionName() {
		return "srm";
	}

	@Override
	public ModelDefinition fromJson(JsonObject o) {
		return GSON.fromJson(o, ModelDefinition.class);
	}

}