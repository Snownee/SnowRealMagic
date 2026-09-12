package snownee.snow.client.model;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.world.level.block.Block;

public record ModelMetadataSection(Identifier model, List<ResourceKey<Block>> overrideBlocks, boolean required) {
	public static final Codec<ModelMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("model").forGetter(ModelMetadataSection::model),
			ResourceKey.codec(Registries.BLOCK)
					.listOf()
					.optionalFieldOf("overrideBlocks", List.of())
					.orElse(List.of())
					.forGetter(ModelMetadataSection::overrideBlocks),
			Codec.BOOL.optionalFieldOf("required", false).forGetter(ModelMetadataSection::required)).apply(instance, ModelMetadataSection::new));

	public static final MetadataSectionType<ModelMetadataSection> TYPE = new MetadataSectionType<>("srm", CODEC);
}
