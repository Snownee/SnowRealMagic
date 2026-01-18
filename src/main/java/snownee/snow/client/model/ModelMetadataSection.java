package snownee.snow.client.model;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public record ModelMetadataSection(Identifier model, List<Identifier> overrideBlocks) {
	private static final Codec<ModelMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("model").forGetter(ModelMetadataSection::model),
			Identifier.CODEC.listOf()
					.optionalFieldOf("override_blocks", List.of())
					.orElse(List.of())
					.forGetter(ModelMetadataSection::overrideBlocks)
	).apply(instance, ModelMetadataSection::new));
	public static final MetadataSectionType<ModelMetadataSection> TYPE = new MetadataSectionType<>("srm", CODEC);
}
