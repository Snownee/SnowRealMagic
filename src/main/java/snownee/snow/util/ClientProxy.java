package snownee.snow.util;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.client.ClientHooks;
import snownee.snow.client.model.ModelMetadataSection;
import snownee.snow.client.model.SnowCoveredModel;
import snownee.snow.client.model.SnowVariantModel;
import snownee.snow.client.model.WrapperUnbakedModel;

public class ClientProxy implements ClientModInitializer {

	public static BlockStateModel getBlockModel(BlockState state) {
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
	}

	public static BlockStateModel getBlockModel(Identifier location) {
		return Minecraft.getInstance().getModelManager().getModel(location);
	}

	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(ctx -> {
			ctx.addModel(ExtraModelKey.create(() -> "TODO"), SimpleUnbakedExtraModel.blockStateModel(ClientHooks.OVERLAY_MODEL));

			ClientHooks.snowVariantMapping.clear();
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			ModelManager.MODEL_LISTER.listMatchingResources(resourceManager).forEach((key, resource) -> {
				ModelMetadataSection def;
				try {
					def = resource.metadata().getSection(ModelMetadataSection.TYPE).orElse(null);
				} catch (IOException e) {
					return;
				}
				if (def == null) {
					return;
				}
				ClientHooks.snowVariantMapping.put(ModelManager.MODEL_LISTER.fileToId(key), def);
				ctx.addModel(ExtraModelKey.create(() -> "TODO"), SimpleUnbakedExtraModel.blockStateModel(def.model()));
				for (Identifier id : def.overrideBlocks()) {
					Block block = BuiltInRegistries.BLOCK.getValue(id);
					if (block != Blocks.AIR) {
						ClientHooks.overrideBlocks.add(block);
					}
				}
			});

			Set<Identifier> snowCoveredModelIds = Sets.newHashSet();
			Map<UnbakedModel, UnbakedModel> transform = Maps.newHashMap();
			for (var block : CommonProxy.allSnowBlocks()) {
				for (BlockState state : block.getStateDefinition().getPossibleStates()) {
					Identifier modelId = BlockModelShaper.stateToModelLocation(BuiltInRegistries.BLOCK.getKey(block), state);
					snowCoveredModelIds.add(modelId);
				}
			}

			ctx.modifyModelOnLoad().register(
					ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
						if (snowCoveredModelIds.contains(context.id())) {
							return transform.computeIfAbsent(model, $ -> new WrapperUnbakedModel($, SnowCoveredModel::new));
						}
						return model;
					});

			ctx.modifyBlockModelAfterBake().register(
					ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
						ModelState modelState = context.settings();
						if (model == null || modelState.getClass() != Variant.class) {
							return model;
						}
						ModelMetadataSection def = ClientHooks.snowVariantMapping.get(context.resourceId());
						if (def == null) {
							return model;
						}
						Variant variantState = (Variant) modelState;
						variantState = new Variant(
								def.model(),
								variantState.getRotation(),
								variantState.isUvLocked(),
								variantState.getWeight());
						BlockStateModel variantModel = context.baker().bake(def.model(), variantState);
						if (variantModel == null) {
							return model;
						}
						return new SnowVariantModel(model, variantModel);
					});

			ClientHooks.cachedOverlayModel = null;
			ClientHooks.cachedSnowModel = null;
		});
	}
}
