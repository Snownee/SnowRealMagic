package snownee.snow.mixin;

import static snownee.snow.CoreModule.*;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.client.ClientVariables;
import snownee.snow.client.model.SnowCoveredModel;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

	@Shadow
	private Map<BlockState, BakedModel> modelByStateCache;
	@Shadow
	private ModelManager modelManager;

	@Inject(at = @At("TAIL"), method = "rebuildCache")
	private void srm_rebuildCache(CallbackInfo ci) {
		Map<BakedModel, SnowCoveredModel> transform = Maps.newHashMap();
		List<Block> allBlocks = List.of(TILE_BLOCK, FENCE, FENCE2, STAIRS, SLAB, FENCE_GATE, WALL);
		for (Block block : allBlocks) {
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				BakedModel model = modelByStateCache.get(state);
				if (model == null || model == modelManager.getMissingModel() || model instanceof SnowCoveredModel) {
					continue;
				}
				SnowCoveredModel snowModel = transform.computeIfAbsent(model, SnowCoveredModel::new);
				modelByStateCache.put(state, snowModel);
			}
		}
		ClientVariables.cachedOverlayModel = null;
		ClientVariables.cachedSnowModel = null;
	}

}
