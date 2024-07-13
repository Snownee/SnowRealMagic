package snownee.snow.util;

import static snownee.snow.CoreModule.FENCE;
import static snownee.snow.CoreModule.FENCE2;
import static snownee.snow.CoreModule.FENCE_GATE;
import static snownee.snow.CoreModule.SLAB;
import static snownee.snow.CoreModule.STAIRS;
import static snownee.snow.CoreModule.TILE_BLOCK;
import static snownee.snow.CoreModule.WALL;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.KiwiGO;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.FallingSnowRenderer;
import snownee.snow.client.SnowClient;
import snownee.snow.client.SnowVariantMetadataSectionSerializer;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowCoveredModel;
import snownee.snow.client.model.SnowVariantModel;

public class ClientProxy {
	public static void init() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.register(ClientProxy.class);
	}

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(CoreModule.ENTITY.get(), FallingSnowRenderer::new);
	}

	@SubscribeEvent
	public static void registerExtraModels(ModelEvent.RegisterAdditional event) {
		event.register(SnowClient.OVERLAY_MODEL);

		SnowClient.snowVariantMapping.clear();
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		ModelBakery.MODEL_LISTER.listMatchingResources(resourceManager).forEach((key, resource) -> {
			ModelDefinition def;
			try {
				def = resource.metadata().getSection(SnowVariantMetadataSectionSerializer.SERIALIZER).orElse(null);
			} catch (IOException e) {
				return;
			}
			if (def == null || def.model == null) {
				return;
			}
			SnowClient.snowVariantMapping.put(ModelBakery.MODEL_LISTER.fileToId(key), def);
			event.register(def.model);
		});
	}

	@SubscribeEvent
	public static void replaceConnected(ModelEvent.ModifyBakingResult event) {
		SnowClient.overrideBlocks.clear();
		for (ModelDefinition def : SnowClient.snowVariantMapping.values()) {
			if (def.overrideBlocks == null) {
				continue;
			}
			for (ResourceLocation override : def.overrideBlocks) {
				Block block = BuiltInRegistries.BLOCK.get(override);
				if (block == Blocks.AIR) {
					SnowRealMagic.LOGGER.error("Cannot handle snow variant override: {}, {}", def.model, override);
					continue;
				}
				SnowClient.overrideBlocks.add(block);
			}
		}
		List<KiwiGO<? extends Block>> allBlocks = List.of(TILE_BLOCK, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL);
		for (KiwiGO<? extends Block> kiwiGO : allBlocks) {
			Block block = kiwiGO.get();
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				ModelResourceLocation modelId = BlockModelShaper.stateToModelLocation(
						ForgeRegistries.BLOCKS.getKey(block),
						state);
				event.getModels().put(modelId, new SnowCoveredModel(event.getModels().get(modelId)));
			}
		}
		SnowClient.cachedOverlayModel = null;
		SnowClient.cachedSnowModel = null;
	}

	public static BakedModel getBlockModel(BlockState state) {
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
	}

	public static BakedModel getBlockModel(ResourceLocation location) {
		return Minecraft.getInstance().getModelManager().getModel(location);
	}

	public static void renderFallingBlock(
			Entity entity,
			BlockState state,
			BlockPos pos,
			PoseStack poseStack,
			MultiBufferSource bufferSource) {
		BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
		poseStack.translate(-0.5D, 0.0D, -0.5D);
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = dispatcher.getBlockModel(state);
		RandomSource random = RandomSource.create(42);
		for (RenderType type : model.getRenderTypes(state, random, ModelData.EMPTY)) {
			dispatcher.getModelRenderer().tesselateBlock(
					entity.level(),
					model,
					state,
					blockpos,
					poseStack,
					bufferSource.getBuffer(type),
					false,
					random,
					state.getSeed(pos),
					OverlayTexture.NO_OVERLAY,
					ModelData.EMPTY,
					type);
		}
	}

	public static BakedModel onBakeModel(
			ResourceLocation resourceLocation,
			ModelState modelState,
			Function<Material, TextureAtlasSprite> sprites,
			ModelBaker modelBaker,
			BakedModel original) {
		if (modelState.getClass() != Variant.class) {
			return null;
		}
		ModelDefinition def = SnowClient.snowVariantMapping.get(resourceLocation);
		if (def == null) {
			return null;
		}
		Variant variantState = (Variant) modelState;
		variantState = new Variant(def.model, variantState.getRotation(), variantState.isUvLocked(), variantState.getWeight());
		BakedModel variantModel = modelBaker.bake(def.model, variantState, sprites);
		return new SnowVariantModel(original, variantModel);
	}
}
