package snownee.snow.mixin;

import static snownee.snow.CoreModule.*;

import java.util.IdentityHashMap;
import java.util.List;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import snownee.kiwi.KiwiGO;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.SnowClient;
import snownee.snow.client.model.ModelDefinition;
import snownee.snow.client.model.SnowConnectedModel;
import snownee.snow.client.model.SnowCoveredModel;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

	@Shadow
	private Map<BlockState, BakedModel> modelByStateCache;
	@Shadow
	private ModelManager modelManager;

	@Inject(at = @At("TAIL"), method = "replaceCache")
	private void srm_replaceCache(Map<BlockState, BakedModel> map, CallbackInfo ci) {
		if (!(modelByStateCache instanceof IdentityHashMap)) {
			modelByStateCache = new IdentityHashMap<>(modelByStateCache);
		}
		Map<BakedModel, BakedModel> transform = Maps.newHashMap();
		List<KiwiGO<? extends Block>> allBlocks = List.of(TILE_BLOCK, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL);
		for (KiwiGO<? extends Block> block : allBlocks) {
			for (BlockState state : block.get().getStateDefinition().getPossibleStates()) {
				BakedModel model = modelByStateCache.get(state);
				if (model == null || model == modelManager.getMissingModel() || model instanceof SnowCoveredModel) {
					continue;
				}
				BakedModel snowModel = transform.computeIfAbsent(model, SnowCoveredModel::new);
				modelByStateCache.put(state, snowModel);
			}
		}
		for (ModelDefinition def : SnowClient.snowVariantMapping.values()) {
			if (def.overrideBlock == null) {
				continue;
			}
			for (ResourceLocation override : def.overrideBlock) {
				Block block = BuiltInRegistries.BLOCK.get(override);
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
		SnowClient.cachedOverlayModel = null;
		SnowClient.cachedSnowModel = null;
	}

}
