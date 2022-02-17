package snownee.snow.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.ClientVariables;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowConnectedModel;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

	@Shadow
	private Map<BlockState, BakedModel> modelByStateCache;
	@Shadow
	private ModelManager modelManager;

	@SuppressWarnings("deprecation")
	@Inject(at = @At("TAIL"), method = "rebuildCache")
	private void srm_rebuildCache(CallbackInfo ci) {
		Map<BakedModel, BakedModel> transform = Maps.newHashMap();
		/*
		List<Block> allBlocks = List.of(TILE_BLOCK, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL);
		for (Block block : allBlocks) {
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				BakedModel model = modelByStateCache.get(state);
				if (model == null || model == modelManager.getMissingModel() || model instanceof SnowCoveredModel) {
					continue;
				}
				BakedModel snowModel = transform.computeIfAbsent(model, SnowCoveredModel::new);
				modelByStateCache.put(state, snowModel);
			}
		}
		*/
		for (ModelDefinition def : ClientVariables.snowVariantMapping.values()) {
			if (def.overrideBlock == null) {
				continue;
			}
			for (ResourceLocation override : def.overrideBlock) {
				Block block = Registry.BLOCK.get(override);
				if (block == null || block == Blocks.AIR) {
					continue;
				}
				if (block.defaultBlockState().hasProperty(DoublePlantBlock.HALF)) {
					for (BlockState state : block.getStateDefinition().getPossibleStates()) {
						if (state.getValue(DoublePlantBlock.HALF) != DoubleBlockHalf.UPPER) {
							continue;
						}
						BakedModel model = modelByStateCache.get(state);
						if (model == null || model == modelManager.getMissingModel() || model instanceof SnowConnectedModel) {
							continue;
						}
						BakedModel snowModel = transform.computeIfAbsent(model, SnowConnectedModel::new);
						modelByStateCache.put(state, snowModel);
					}
					continue;
				}
				SnowRealMagic.LOGGER.error("Cannot handle snow variant override: {}, {}", def.model, override);
			}
		}
		ClientVariables.cachedOverlayModel = null;
		ClientVariables.cachedSnowModel = null;
	}

}