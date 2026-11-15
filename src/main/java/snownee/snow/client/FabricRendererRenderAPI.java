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
import snownee.snow.block.SnowVariant;

public class FabricRendererRenderAPI implements RenderAPI {

	private static final BlockState TOP_SLAB = Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);

	private final QuadEmitter emitter;
	private final BlockAndTintGetter level;
	private final BlockState selfState;
	private final RandomSource random;
	private final BlockPos pos;
	private final Predicate<@Nullable Direction> cullTest;
	private final BlockStateModel unwrapped;
	private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
	private int cullCacheValid;
	private int cullCacheCulled;

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
		cullCacheValid = 0;
		cullCacheCulled = 0;
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
			if (part == ModelPart.CAMO) {
				Direction cullFace = quad.cullFace();
				if (cullFace != null) {
					// The renderer would cull this quad with the state of the snow layer block, which is not the
					// geometry rendered here. Clear it and cull with the states rendered at the neighbor instead.
					Direction nominalFace = quad.nominalFace();
					quad.cullFace(null);
					quad.nominalFace(nominalFace);
					int mask = 1 << cullFace.ordinal();
					if ((cullCacheValid & mask) == 0) {
						cullCacheValid |= mask;
						if (shouldCullCamoFace(blockState, cullFace)) {
							cullCacheCulled |= mask;
						}
					}
					if ((cullCacheCulled & mask) != 0) {
						return false;
					}
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

		var cullTest = newCullTest(blockState, part, offset);

		((FabricBlockStateModel) model).emitQuads(emitter, level, pos, blockState, random, cullTest);
		emitter.popTransform();
		return true;
	}

	/**
	 * Culls a camo face with the geometry the neighbor actually renders, i.e. its camo and its snow layer instead
	 * of the snow layer block itself. Using the snow layer block would cull the face of a snow-covered vertical
	 * slab that is next to a snow layer of the same height, while leaving the face between two snow-covered
	 * vertical slabs unculled.
	 */
	private boolean shouldCullCamoFace(BlockState camo, Direction direction) {
		BlockPos neighborPos = scratchPos.setWithOffset(pos, direction);
		BlockState neighborState = level.getBlockState(neighborPos);
		if (neighborState.getBlock() instanceof SnowVariant snowVariant) {
			BlockState rawState = snowVariant.srm$getRaw(neighborState, level, neighborPos);
			if (!rawState.isAir() && !Block.shouldRenderFace(camo, rawState, direction)) {
				return true;
			}
			BlockState snowState = snowVariant.srm$getSnowState(neighborState, level, neighborPos);
			return !snowState.isAir() && !Block.shouldRenderFace(camo, snowState, direction);
		}
		return !Block.shouldRenderFace(camo, neighborState, direction);
	}

	private Predicate<@Nullable Direction> newCullTest(BlockState blockState, ModelPart part, Vec3 offset) {
		var cullTest = this.cullTest;
		if (part == ModelPart.CAMO) {
			// Camo faces are culled in the transform, where the geometry rendered by the neighbor is known.
			return $ -> false;
		}
		boolean generalOverlay = part == ModelPart.SNOW_OVERLAY && offset.y <= -1.0;
		if (part == ModelPart.DECORATION || part == ModelPart.SNOW_OVERLAY || part == ModelPart.SNOW_LAYER) {
			cullTest = $ -> {
				if (this.cullTest.test($)) {
					return true;
				}
				if ($ == null) {
					return false;
				}
				BlockPos.MutableBlockPos pos1 = this.scratchPos.setWithOffset(pos, $);
				BlockState selfState = blockState;
				if (generalOverlay) {
					pos1.move(Direction.DOWN);
					selfState = TOP_SLAB;
				} else if (part == ModelPart.SNOW_LAYER && CoreModule.SLAB.is(this.selfState)) {
					selfState = this.selfState;
				}
				BlockState neighborState = level.getBlockState(pos1);
				return !Block.shouldRenderFace(selfState, neighborState, $);
			};
		}
		return cullTest;
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
