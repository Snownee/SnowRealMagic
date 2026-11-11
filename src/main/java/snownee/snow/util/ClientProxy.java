package snownee.snow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.JsonOps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.SnowAccumulation;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.ClientHooks;
import snownee.snow.client.SnowyLeaves;
import snownee.snow.client.SnowyLeavesSource;
import snownee.snow.client.model.ModelMetadataSection;
import snownee.snow.client.model.SnowCoveredModel;
import snownee.snow.client.model.SnowVariantModel;
import snownee.snow.client.model.SnowyLeavesModel;

public class ClientProxy implements ClientModInitializer {

	public static final ExtraModelKey<BlockStateModel> OVERLAY_MODEL = ExtraModelKey.create(() -> "SRM Snow Overlay Model");

	public static BlockStateModel getBlockModel(BlockState state) {
		return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
	}

	/**
	 * Returns the raw geometry of the block's model. {@link SnowCoveredModel} only works during a real block render
	 * (it needs the block entity render data), so the destroy overlay must use the wrapped model instead.
	 */
	public static BlockStateModel getUnwrappedBlockModel(BlockState state) {
		BlockStateModel model = getBlockModel(state);
		return model instanceof SnowCoveredModel snowCoveredModel ? snowCoveredModel.srm$getWrapped() : model;
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
		SpriteSourceRegistry.register(SnowRealMagic.id("snowy_leaves"), SnowyLeavesSource.CODEC);
		ClientChunkEvents.CHUNK_LOAD.register(SnowyLeaves::onChunkLoad);
		ClientChunkEvents.CHUNK_UNLOAD.register(SnowyLeaves::onChunkUnload);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> SnowyLeaves.clear());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SnowyLeaves.clear());
		ClientTickEvents.END_CLIENT_TICK.register(client -> SnowyLeaves.tick());

		// TODO: temporary debug feature. Send an integer 0-100 in chat to change the snowy leaves intensity.
/*		ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
			String trimmed = message.trim();
			if (trimmed.length() > 3) {
				return true;
			}
			int value;
			try {
				value = Integer.parseInt(trimmed);
			} catch (NumberFormatException e) {
				return true;
			}
			if (value < 0 || value > 100) {
				return true;
			}
			ConfigHandler handler = KiwiConfigManager.getHandler(SnowClientConfig.class);
			ConfigHandler.Value<Integer> configValue = handler.get("visual.snowyLeavesIntensity");
			configValue.accept(value);
			handler.save();
			Minecraft.getInstance().player.sendSystemMessage(Component.literal("Snowy leaves intensity: " + value));
			return false;
		});*/

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
							return interner.intern(new SnowVariantModel(
									model,
									ClientHooks.requiredOverrideBlocks.contains(context.state().getBlock())));
						});
			}

			{
				Interner<BlockStateModel> interner = Interners.newStrongInterner();
				ctx.modifyBlockModelAfterBake().register(
						ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
							if (model instanceof SnowyLeavesModel) {
								return model;
							}
							SnowyLeaves.Leaf leaf = SnowyLeaves.leaf(context.state().getBlock());
							if (leaf == null) {
								return model;
							}
							MaterialBaker materials = context.baker().materials();
							ModelDebugName debugName = () -> "snowy_leaves";
							TextureAtlasSprite base = model.particleMaterial().sprite();
							Identifier baseTexture = base.contents().name();
							TextureAtlasSprite[] tops = new TextureAtlasSprite[SnowAccumulation.MAX_STATE + 1];
							TextureAtlasSprite[] sides = new TextureAtlasSprite[SnowAccumulation.MAX_STATE + 1];
							for (int level = 0; level <= SnowAccumulation.MAX_STATE; level++) {
								if (SnowyLeaves.intensityForLevel(level) <= 0) {
									// SnowyLeavesSource does not generate these, and the renderer never uses them.
									tops[level] = base;
									sides[level] = base;
									continue;
								}
								Identifier topId = SnowyLeaves.snowySprite(leaf.block(), baseTexture, false, level);
								Identifier sideId = SnowyLeaves.snowySprite(leaf.block(), baseTexture, true, level);
								tops[level] = materials.get(new Material(topId), debugName).sprite();
								sides[level] = materials.get(new Material(sideId), debugName).sprite();
								if (!tops[level].contents().name().equals(topId) || !sides[level].contents().name().equals(sideId)) {
									return model;
								}
							}
							return interner.intern(new SnowyLeavesModel(model, base, tops, sides));
						});
			}

			ClientHooks.cachedOverlayModel = null;
			ClientHooks.cachedSnowModel = null;
		});
	}

	private static void addModelDefinition(Identifier id, @Nullable ModelMetadataSection section) {
		if (section == null) {
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
