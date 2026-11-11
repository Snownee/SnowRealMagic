package snownee.snow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import snownee.snow.SnowAccumulation;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.ClientHooks;
import snownee.snow.client.SnowyLeaves;
import snownee.snow.client.SnowyLeavesSource;
import snownee.snow.client.model.ModelMetadataSection;
import snownee.snow.client.model.SnowCoveredModel;
import snownee.snow.client.model.SnowVariantModel;
import snownee.snow.client.model.SnowyLeavesModel;

public class ClientProxy {

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
		BlockStateModel model = ((FabricModelManager) modelManager).getModel((ExtraModelKey<BlockStateModel>) key);
		return model != null ? model : modelManager.getBlockStateModelSet().missingModel();
	}

	public static void onInitializeClient(IEventBus eventBus) {
		SpriteSourceRegistry.register(SnowRealMagic.id("snowy_leaves"), SnowyLeavesSource.CODEC);
		NeoForge.EVENT_BUS.addListener(ChunkEvent.Load.class, event -> {
			if (event.getLevel() instanceof ClientLevel level) {
				SnowyLeaves.onChunkLoad(level, event.getChunk());
			}
		});
		NeoForge.EVENT_BUS.addListener(ChunkEvent.Unload.class, event -> {
			if (event.getLevel() instanceof ClientLevel level) {
				SnowyLeaves.onChunkUnload(level, event.getChunk());
			}
		});
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> SnowyLeaves.clear());
		NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> SnowyLeaves.clear());
		NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> SnowyLeaves.tick());

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

		eventBus.addListener(EventPriority.LOW, ModelEvent.ModifyBakingResult.class, event -> {
			Set<Block> snowBlocks = Set.copyOf(CommonProxy.allSnowBlocks());
			event.getBakingResult().blockStateModels().replaceAll((state, model) -> {
				if (!snowBlocks.contains(state.getBlock()) || model instanceof SnowCoveredModel) {
					return model;
				}
				return new SnowCoveredModel(model);
			});
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
