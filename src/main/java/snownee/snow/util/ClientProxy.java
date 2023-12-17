package snownee.snow.util;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.FallingSnowRenderer;
import snownee.snow.client.SnowClient;
import snownee.snow.client.SnowVariantMetadataSectionSerializer;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowConnectedModel;

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
		Map<ResourceLocation, BakedModel> models = event.getModels();
		Map<BakedModel, BakedModel> transform = Maps.newHashMap();
		for (ModelDefinition def : SnowClient.snowVariantMapping.values()) {
			if (def.overrideBlock == null) {
				continue;
			}
			for (ResourceLocation override : def.overrideBlock) {
				Block block = BuiltInRegistries.BLOCK.get(override);
				if (block == Blocks.AIR || !block.defaultBlockState().hasProperty(DoublePlantBlock.HALF)) {
					SnowRealMagic.LOGGER.error("Cannot handle snow variant override: {}, {}", def.model, override);
					continue;
				}
				for (BlockState state : block.getStateDefinition().getPossibleStates()) {
					if (state.getValue(DoublePlantBlock.HALF) != DoubleBlockHalf.UPPER) {
						continue;
					}
					ModelResourceLocation location = BlockModelShaper.stateToModelLocation(override, state);
					BakedModel model = models.get(location);
					if (model == null || model instanceof SnowConnectedModel) {
						continue;
					}
					BakedModel snowModel = transform.computeIfAbsent(model, SnowConnectedModel::new);
					models.put(location, snowModel);
				}
			}
		}
		SnowClient.cachedOverlayModel = null;
		SnowClient.cachedSnowModel = null;
	}
}
