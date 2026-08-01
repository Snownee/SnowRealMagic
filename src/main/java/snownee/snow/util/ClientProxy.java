package snownee.snow.util;

import java.io.IOException;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import net.neoforged.bus.api.IEventBus;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.client.ClientHooks;
import snownee.snow.client.model.ModelMetadataSection;
import snownee.snow.client.model.SnowCoveredModel;
import snownee.snow.client.model.SnowVariantModel;

public class ClientProxy {

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
		BlockStateModel model = ((FabricModelManager) modelManager).getModel((ExtraModelKey<BlockStateModel>) key);
		return model != null ? model : modelManager.getBlockStateModelSet().missingModel();
	}

	public static void onInitializeClient(IEventBus eventBus) {
		ModelLoadingPlugin.register(ctx -> {
			ctx.addModel(OVERLAY_MODEL, SimpleUnbakedExtraModel.blockStateModel(ClientHooks.OVERLAY_MODEL));

			ClientHooks.snowVariantMapping.clear();
			ClientHooks.overrideBlocks.clear();
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			ModelManager.MODEL_LISTER.listMatchingResources(resourceManager).forEach((key, resource) -> {
				ModelMetadataSection section;
				try {
					section = resource.metadata().getSection(ModelMetadataSection.TYPE).orElse(null);
				} catch (IOException e) {
					return;
				}
				if (section == null) {
					return;
				}
				ClientHooks.snowVariantMapping.put(ModelManager.MODEL_LISTER.fileToId(key), section);
				for (ResourceKey<Block> id : section.overrideBlocks()) {
					Block block = BuiltInRegistries.BLOCK.getValue(id);
					if (block != Blocks.AIR && block != null) {
						ClientHooks.overrideBlocks.add(block);
					}
				}
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
							return interner.intern(new SnowVariantModel(model));
						});
			}

			ClientHooks.cachedOverlayModel = null;
			ClientHooks.cachedSnowModel = null;
		});
	}
}
