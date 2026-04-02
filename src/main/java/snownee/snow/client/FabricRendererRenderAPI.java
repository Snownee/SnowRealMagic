package snownee.snow.client;

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import snownee.snow.CoreModule;

public class FabricRendererRenderAPI implements RenderAPI {

	private static final BlockState TOP_SLAB = Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);

	private final QuadEmitter emitter;
	private final BlockAndTintGetter level;
	private final BlockState selfState;
	private final RandomSource random;
	private final BlockPos pos;
	private final Predicate<@Nullable Direction> cullTest;
	private final BlockStateModel unwrapped;


	public FabricRendererRenderAPI(
			QuadEmitter emitter,
			BlockAndTintGetter level,
			BlockPos pos,
			BlockState selfState,
			RandomSource random,
			Predicate<@Nullable Direction> cullTest,
			BlockStateModel wrapped) {
		this.emitter = emitter;
		this.level = level;
		this.pos = pos;
		this.selfState = selfState;
		this.random = random;
		this.cullTest = cullTest;
		this.unwrapped = wrapped;
	}

	@Override
	public boolean render(BlockState blockState, BlockStateModel model, double yOffset, ModelPart part) {
		Vec3 offset = yOffset == 0 ? blockState.getOffset(pos) : blockState.getOffset(pos).add(0, yOffset, 0);
		boolean expandModel = part == ModelPart.CAMO && blockState.is(CoreModule.EXPAND_MODEL);
		emitter.pushTransform(quad -> {
			if (part == ModelPart.SNOW_LAYER && yOffset != 0) { // is slab
				if (quad.nominalFace() == Direction.DOWN) {
					return false;
				} else if (quad.cullFace() != null) {
					quad.cullFace(null);
				}
			}
			if (part == ModelPart.CAMO && quad.nominalFace() == Direction.UP) {
				Block block = blockState.getBlock();
				if (block instanceof StairBlock || block instanceof SlabBlock || block instanceof WallBlock) {
					return false;
				}
			}
			int color = -1;
			if (quad.tintIndex() != -1) {
				BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(blockState, quad.tintIndex());
				if (tintSource != null) {
					color = tintSource.colorInWorld(blockState, level, pos) | 0xFF000000;
				}
			}
			if (expandModel || offset != Vec3.ZERO || color != -1) {
				for (int i = 0; i < 4; ++i) {
					float x = quad.x(i) + (float) offset.x;
					float y = quad.y(i) + (float) offset.y;
					float z = quad.z(i) + (float) offset.z;
					if (expandModel) {
						x = expandModel(x);
						y = expandModel(y);
						z = expandModel(z);
					}
					quad.pos(i, x, y, z);
					quad.color(i, color);
				}
			}
			return true;
		});
		if (blockState == selfState && model != ClientHooks.cachedOverlayModel) {
			model = unwrapped;
		}
		if (context instanceof AbstractBlockRenderContextAccess blockRenderContext) {
			BlockRenderInfo blockInfo = blockRenderContext.getBlockInfo();
			boolean generalOverlay = part == ModelPart.SNOW_OVERLAY && offset.y <= -1.0;
			blockInfo.prepareForBlock(pos, generalOverlay ? TOP_SLAB : blockState);
			if (generalOverlay) {
				((BlockRenderInfoAccess) blockInfo).setDefaultLayer(ChunkSectionLayer.CUTOUT);
				blockInfo.blockPos = pos.below();
				for (Direction direction : Direction.Plane.HORIZONTAL) {
					emitter.cullFace(direction);
				}
				blockInfo.blockPos = pos;
			}
		}
		((FabricBlockStateModel) model).emitQuads(emitter, level, pos, blockState, random, cullTest);
		emitter.popTransform();
		return true;
	}

	private static float expandModel(float f) {
		if (Mth.equal(f, 0f)) {
			return f - 0.001f;
		}
		if (Mth.equal(f, 1f)) {
			return f + 0.001f;
		}
		return f;
	}

	@Override
	public BlockAndTintGetter level() {
		return level;
	}

	@Override
	public BlockPos pos() {
		return pos;
	}

}
