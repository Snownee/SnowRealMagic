package snownee.snow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.JsonOps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.ClientHooks;
import snownee.snow.client.model.ModelMetadataSection;
import snownee.snow.client.model.SnowCoveredModel;
import snownee.snow.client.model.SnowVariantModel;

public class ClientProxy implements ClientModInitializer {

	public static final ExtraModelKey<BlockStateModel> OVERLAY_MODEL = ExtraModelKey.create(() -> "SRM Snow Overlay Model");

	public static BlockStateModel getBlockModel(BlockState state) {
		return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
	}

	public static BlockStateModel getExtraBlockModel(@Nullable Object key) {
		ModelManager modelManager = Minecraft.getInstance().getModelManager();
		if (key == null) {
			return modelManager.getBlockStateModelSet().missingModel();
		}
		@SuppressWarnings("unchecked")
		BlockStateModel model = modelManager.getModel((ExtraModelKey<BlockStateModel>) key);
		return model != null ? model : modelManager.getBlockStateModelSet().missingModel();
	}

	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(ctx -> {
			ctx.addModel(OVERLAY_MODEL, SimpleUnbakedExtraModel.blockStateModel(ClientHooks.OVERLAY_MODEL));

			ClientHooks.snowVariantMapping.clear();
			ClientHooks.overrideBlocks.clear();
			ClientHooks.requiredOverrideBlocks.clear();
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			ModelManager.MODEL_LISTER.listMatchingResources(resourceManager).forEach((key, resource) -> {
				ModelMetadataSection section;
				try {
					section = resource.metadata().getSection(ModelMetadataSection.TYPE).orElse(null);
				} catch (IOException e) {
					return;
				}
				addModelDefinition(ModelManager.MODEL_LISTER.fileToId(key), section);
			});
			FileToIdConverter srmVariantLister = new FileToIdConverter("srm_variants", ".json");
			srmVariantLister.listMatchingResources(resourceManager).forEach((key, resource) -> {
				ModelMetadataSection section;
				try (BufferedReader reader = resource.openAsReader()) {
					section = ModelMetadataSection.CODEC.parse(JsonOps.INSTANCE, GsonHelper.parse(reader))
							.resultOrPartial(SnowRealMagic.LOGGER::error)
							.orElse(null);
				} catch (IOException e) {
					return;
				}
				addModelDefinition(srmVariantLister.fileToId(key), section);
			});

			{
				Set<Block> snowBlocks = Set.copyOf(CommonProxy.allSnowBlocks());
				Interner<BlockStateModel> interner = Interners.newStrongInterner();
				ctx.modifyBlockModelAfterBake().register(
						ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
							if (!snowBlocks.contains(context.state().getBlock()) || model instanceof SnowCoveredModel) {
								return model;
							}
							return interner.intern(new SnowCoveredModel(model));
						});
			}

			{
				Interner<BlockStateModel> interner = Interners.newStrongInterner();
				ctx.modifyBlockModelAfterBake().register(
						ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
							if (!ClientHooks.overrideBlocks.contains(context.state().getBlock()) || model instanceof SnowVariantModel) {
								return model;
							}
							return interner.intern(new SnowVariantModel(model, ClientHooks.requiredOverrideBlocks.contains(context.state().getBlock())));
						});
			}

			ClientHooks.cachedOverlayModel = null;
			ClientHooks.cachedSnowModel = null;
		});
	}

	private static void addModelDefinition(Identifier id, @Nullable ModelMetadataSection section) {
		if (section == null || section.model() == null) {
			return;
		}
		ClientHooks.snowVariantMapping.put(id, section);
		for (ResourceKey<Block> blockId : section.overrideBlocks()) {
			Block block = BuiltInRegistries.BLOCK.getValue(blockId);
			if (block != Blocks.AIR && block != null) {
				ClientHooks.overrideBlocks.add(block);
				if (section.required()) {
					ClientHooks.requiredOverrideBlocks.add(block);
				}
			}
		}
	}
}
