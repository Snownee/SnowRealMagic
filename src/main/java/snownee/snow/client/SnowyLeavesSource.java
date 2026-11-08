package snownee.snow.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import snownee.snow.SnowAccumulation;
import snownee.snow.SnowRealMagic;

public final class SnowyLeavesSource implements SpriteSource {

	public static final MapCodec<SnowyLeavesSource> CODEC = MapCodec.unit(new SnowyLeavesSource());

	private static final int DEFAULT_SNOWY_FOLIAGE_COLOR = 0xFF60A17B;
	private static final Identifier SNOW_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "block/snow");

	@Override
	public void run(ResourceManager resourceManager, Output output) {
		int snowyTint = readSnowyFoliageColor(resourceManager);
		int generated = 0;
		Map<Identifier, UnbakedModel> modelCache = new HashMap<>();
		try (NativeImage snow = readTexture(resourceManager, SNOW_TEXTURE)) {
			for (Block block : SnowAccumulation.supportedBlocks()) {
				SnowyLeaves.Leaf leaf = SnowyLeaves.leaf(block);
				if (leaf == null) {
					continue;
				}
				for (Identifier texture : resolveBaseTextures(resourceManager, block, modelCache)) {
					Identifier textureResource = Identifier.fromNamespaceAndPath(
							texture.getNamespace(),
							"textures/" + texture.getPath() + ".png");
					Optional<Resource> optional = resourceManager.getResource(textureResource);
					if (optional.isEmpty()) {
						SnowRealMagic.LOGGER.warn("Missing leaf texture {}, skipping its snowy variant", textureResource);
						continue;
					}
					Resource resource = optional.get();
					try (InputStream in = resource.open(); NativeImage source = NativeImage.read(in)) {
						int frameWidth = source.getWidth();
						int frameHeight = source.getHeight();
						ResourceMetadata metadata = resource.metadata();
						Optional<AnimationMetadataSection> animation = metadata.getSection(AnimationMetadataSection.TYPE);
						if (animation.isPresent()) {
							FrameSize frameSize = animation.get().calculateFrameSize(frameWidth, frameHeight);
							frameWidth = frameSize.width();
							frameHeight = frameSize.height();
						}
						Optional<TextureMetadataSection> textureMetadata = metadata.getSection(TextureMetadataSection.TYPE);
						int tint = leaf.biomeTint() ? snowyTint : leaf.tint();
						LuminanceRange range = computeLuminanceRange(source, frameWidth, frameHeight);
						for (int level = 0; level <= SnowAccumulation.MAX_STATE; level++) {
							int intensity = SnowyLeaves.intensityForLevel(level);
							if (intensity <= 0) {
								// Such a texture would be an untinted copy of the source texture and is never rendered.
								continue;
							}
							NativeImage top = generate(source, snow, frameWidth, frameHeight, tint, intensity, false, range);
							NativeImage side = generate(source, snow, frameWidth, frameHeight, tint, intensity, true, range);
							add(output, SnowyLeaves.snowySprite(block, texture, false, level), frameWidth, frameHeight, top, textureMetadata);
							add(output, SnowyLeaves.snowySprite(block, texture, true, level), frameWidth, frameHeight, side, textureMetadata);
							generated++;
						}
					} catch (IOException e) {
						SnowRealMagic.LOGGER.warn("Failed to generate snowy variant for {}", textureResource, e);
					}
				}
			}
		}
		SnowRealMagic.LOGGER.info(
				"Generated snowy variants for {} blocks at minimum intensity {}",
				generated,
				SnowClientConfig.snowyLeavesIntensity);
	}

	@Override
	public MapCodec<? extends SpriteSource> codec() {
		return CODEC;
	}

	private static List<Identifier> resolveBaseTextures(
			ResourceManager resourceManager,
			Block block,
			Map<Identifier, UnbakedModel> modelCache) {
		Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
		Set<Identifier> textures = new LinkedHashSet<>();
		for (Identifier modelId : readBlockStateModels(resourceManager, blockId)) {
			Identifier texture = readParticleTexture(resourceManager, modelId, modelCache);
			if (texture != null) {
				textures.add(texture);
			}
		}
		if (textures.isEmpty()) {
			SnowRealMagic.LOGGER.warn("Unable to resolve the model texture of {}, skipping its snowy variants", blockId);
		}
		return List.copyOf(textures);
	}

	private static Set<Identifier> readBlockStateModels(ResourceManager resourceManager, Identifier blockId) {
		Identifier file = Identifier.fromNamespaceAndPath(
				blockId.getNamespace(),
				"blockstates/" + blockId.getPath() + ".json");
		Optional<Resource> optional = resourceManager.getResource(file);
		if (optional.isEmpty()) {
			SnowRealMagic.LOGGER.warn("Missing blockstate {}, skipping its snowy variants", file);
			return Set.of();
		}
		Set<Identifier> models = new LinkedHashSet<>();
		try (Reader reader = optional.get().openAsReader()) {
			JsonObject json = GsonHelper.parse(reader);
			JsonObject variants = json.getAsJsonObject("variants");
			if (variants != null) {
				for (Map.Entry<String, JsonElement> entry : variants.entrySet()) {
					collectModels(entry.getValue(), models);
				}
			}
			JsonArray multipart = json.getAsJsonArray("multipart");
			if (multipart != null) {
				for (JsonElement element : multipart) {
					JsonElement apply = element.getAsJsonObject().get("apply");
					if (apply != null) {
						collectModels(apply, models);
					}
				}
			}
		} catch (Exception e) {
			SnowRealMagic.LOGGER.warn("Failed to parse blockstate {}", file, e);
		}
		return models;
	}

	private static void collectModels(JsonElement element, Set<Identifier> models) {
		if (element.isJsonArray()) {
			for (JsonElement child : element.getAsJsonArray()) {
				collectModels(child, models);
			}
		} else if (element.isJsonObject()) {
			JsonElement model = element.getAsJsonObject().get("model");
			if (model != null) {
				models.add(Identifier.parse(model.getAsString()));
			}
		}
	}

	private static @Nullable Identifier readParticleTexture(
			ResourceManager resourceManager,
			Identifier modelId,
			Map<Identifier, UnbakedModel> modelCache) {
		TextureSlots.Resolver resolver = new TextureSlots.Resolver();
		Identifier current = modelId;
		for (int depth = 0; current != null && depth < 64; depth++) {
			UnbakedModel model = loadModel(resourceManager, current, modelCache);
			if (model == null) {
				return null;
			}
			resolver.addLast(model.textureSlots());
			current = model.parent();
		}
		ModelDebugName debugName = () -> "snowy_leaves/" + modelId;
		Material material = resolver.resolve(debugName).getMaterial(UnbakedModel.PARTICLE_TEXTURE_REFERENCE);
		return material == null ? null : material.sprite();
	}

	private static @Nullable UnbakedModel loadModel(
			ResourceManager resourceManager,
			Identifier modelId,
			Map<Identifier, UnbakedModel> modelCache) {
		if (modelCache.containsKey(modelId)) {
			return modelCache.get(modelId);
		}
		Optional<UnbakedModel> parsed = parseModel(resourceManager, modelId);
		if (parsed.isEmpty()) {
			return null;
		}
		UnbakedModel model = parsed.get();
		modelCache.put(modelId, model);
		return model;
	}

	private static Optional<UnbakedModel> parseModel(ResourceManager resourceManager, Identifier modelId) {
		Identifier file = Identifier.fromNamespaceAndPath(
				modelId.getNamespace(),
				"models/" + modelId.getPath() + ".json");
		Optional<Resource> optional = resourceManager.getResource(file);
		if (optional.isEmpty()) {
			SnowRealMagic.LOGGER.warn("Missing model {}, skipping its snowy variants", file);
			return Optional.empty();
		}
		try (Reader reader = optional.get().openAsReader()) {
			return Optional.of(UnbakedModelDeserializer.deserialize(reader));
		} catch (Exception e) {
			SnowRealMagic.LOGGER.warn("Failed to parse model {}", file, e);
			return Optional.empty();
		}
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static void add(
			Output output,
			Identifier id,
			int width,
			int height,
			NativeImage image,
			Optional<TextureMetadataSection> textureMetadata) {
		SpriteContents contents = new SpriteContents(
				id,
				new FrameSize(width, height),
				image,
				Optional.empty(),
				List.of(),
				textureMetadata);
		output.add(id, loader -> contents);
	}

	private static int readSnowyFoliageColor(ResourceManager resourceManager) {
		Identifier id = Identifier.fromNamespaceAndPath("minecraft", "textures/colormap/foliage.png");
		Optional<Resource> optional = resourceManager.getResource(id);
		if (optional.isEmpty()) {
			return DEFAULT_SNOWY_FOLIAGE_COLOR;
		}
		try (InputStream in = optional.get().open(); NativeImage image = NativeImage.read(in)) {
			// Temperature 0 and downfall 0.5 map to the bottom-right corner of the vanilla colormap.
			int x = Math.min(255, image.getWidth() - 1);
			int y = Math.min(255, image.getHeight() - 1);
			return image.getPixel(x, y) | 0xFF000000;
		} catch (IOException e) {
			SnowRealMagic.LOGGER.warn("Failed to read the foliage colormap", e);
			return DEFAULT_SNOWY_FOLIAGE_COLOR;
		}
	}

	private static @Nullable NativeImage readTexture(ResourceManager resourceManager, Identifier id) {
		Identifier resourceId = Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png");
		Optional<Resource> optional = resourceManager.getResource(resourceId);
		if (optional.isEmpty()) {
			return null;
		}
		try (InputStream in = optional.get().open()) {
			return NativeImage.read(in);
		} catch (IOException e) {
			SnowRealMagic.LOGGER.warn("Failed to read texture {}", resourceId, e);
			return null;
		}
	}

	private record LuminanceRange(float min, float max) {
		private float normalize(float luminance) {
			if (max <= min) {
				return 1F;
			}
			return Math.max(0F, Math.min(1F, (luminance - min) / (max - min)));
		}
	}

	private static LuminanceRange computeLuminanceRange(NativeImage source, int width, int height) {
		float min = 1F;
		float max = 0F;
		boolean found = false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = source.getPixel(x, y);
				if ((pixel >>> 24) == 0) {
					continue;
				}
				float luminance = luminance(pixel);
				min = Math.min(min, luminance);
				max = Math.max(max, luminance);
				found = true;
			}
		}
		return found ? new LuminanceRange(min, max) : new LuminanceRange(0F, 1F);
	}

	private static float luminance(int pixel) {
		int r = (pixel >> 16) & 0xFF;
		int g = (pixel >> 8) & 0xFF;
		int b = pixel & 0xFF;
		return (0.299F * r + 0.587F * g + 0.114F * b) / 255F;
	}

	private static NativeImage generate(
			NativeImage source,
			@Nullable NativeImage snow,
			int width,
			int height,
			int tint,
			int intensity,
			boolean side,
			LuminanceRange range) {
		NativeImage result = new NativeImage(width, height, false);
		float strength = intensity / 100F;
		float minOpacity = SnowClientConfig.snowyLeavesMinOpacity / 100F;
		float influence = SnowClientConfig.snowyLeavesBrightnessInfluence / 100F;
		// The side gradient weakens as the overall intensity grows, so full snow covers the sides more evenly.
		float sideGradient = SnowClientConfig.snowyLeavesSideGradient / 100F * (1F - strength);
		int sideGradientSteps = SnowClientConfig.snowyLeavesSideGradientSteps;
		// Boost the top of the side faces at low intensity with a gamma curve.
		float sideTopBoost = SnowClientConfig.snowyLeavesSideTopBoost / 100F;
		float sideStrength = (float) Math.pow(strength, 1F - sideTopBoost * 0.5F);
		int tintR = tint >> 16 & 0xFF;
		int tintG = tint >> 8 & 0xFF;
		int tintB = tint & 0xFF;
		int snowWidth = snow != null ? snow.getWidth() : 1;
		int snowHeight = snow != null ? snow.getHeight() : 1;

		for (int y = 0; y < height; y++) {
			float rowBlend = side ? sideStrength : strength;
			if (side) {
				// Snow is thickest at the top of the side faces and fades out towards the bottom.
				float sideFactor = 1F - sideGradient * (height > 1 ? y / (float) (height - 1) : 0F);
				if (sideGradientSteps > 1) {
					sideFactor = Math.round(sideFactor * (sideGradientSteps - 1)) / (float) (sideGradientSteps - 1);
				} else {
					sideFactor = 1F;
				}
				rowBlend *= sideFactor;
			}
			int snowY = Math.min(snowHeight - 1, y * snowHeight / height);
			for (int x = 0; x < width; x++) {
				int pixel = source.getPixel(x, y);
				if ((pixel >>> 24) == 0) {
					result.setPixel(x, y, 0);
					continue;
				}
				int r = ((pixel >> 16) & 0xFF) * tintR / 255;
				int g = ((pixel >> 8) & 0xFF) * tintG / 255;
				int b = (pixel & 0xFF) * tintB / 255;
				float brightness = range.normalize(luminance(pixel));
				float brightnessFactor = minOpacity + (1F - minOpacity) * (1F - influence + influence * brightness);
				float blend = rowBlend * brightnessFactor;
				if (blend > 0F) {
					int snowX = Math.min(snowWidth - 1, x * snowWidth / width);
					int snowPixel = snow != null ? snow.getPixel(snowX, snowY) : 0xFFFFFFFF;
					r = lerp(r, (snowPixel >> 16) & 0xFF, blend);
					g = lerp(g, (snowPixel >> 8) & 0xFF, blend);
					b = lerp(b, snowPixel & 0xFF, blend);
				}
				result.setPixel(x, y, 0xFF000000 | r << 16 | g << 8 | b);
			}
		}
		return result;
	}

	private static int lerp(int from, int to, float delta) {
		return Math.round(from + (to - from) * delta);
	}
}
