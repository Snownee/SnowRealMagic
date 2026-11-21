package snownee.snow.client;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;
import snownee.snow.mixin.client.AbstractBlockRenderContextAccess;
import snownee.snow.mixin.client.BlockRenderInfoAccess;

public class FabricRendererRenderAPI implements RenderAPI {

	private static final BlockState TOP_SLAB = Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
	private final BlockAndTintGetter level;
	private final RenderContext context;
	private final @Nullable RenderType renderType;
	private final Supplier<RandomSource> randomSupplier;
	private final BlockState selfState;
	private final BlockPos pos;
	private final BakedModel unwrapped;
	private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
	private int cullCacheValid;
	private int cullCacheCulled;

	public FabricRendererRenderAPI(
			BlockAndTintGetter level,
			RenderContext context,
			@Nullable RenderType renderType,
			Supplier<RandomSource> randomSupplier,
			BlockState selfState,
			BlockPos pos,
			BakedModel unwrapped) {
		this.level = level;
		this.context = context;
		this.renderType = renderType;
		this.randomSupplier = randomSupplier;
		this.selfState = selfState;
		this.pos = pos;
		this.unwrapped = unwrapped;
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public boolean render(BlockState blockState, BakedModel model, double yOffset, ModelPart part) {
		RandomSource random = randomSupplier.get();
		if (renderType != null && !model.getRenderTypes(blockState, random, context.getModelData()).contains(renderType)) {
			return false;
		}

		Vec3 offset = yOffset == 0 ? blockState.getOffset(level, pos) : blockState.getOffset(level, pos).add(0, yOffset, 0);
		boolean expandModel = part == ModelPart.CAMO && blockState.is(CoreModule.EXPAND_MODEL);
		cullCacheValid = 0;
		cullCacheCulled = 0;
		context.pushTransform(quad -> {
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
			if (quad.colorIndex() != -1) {
				color = Minecraft.getInstance().getBlockColors().getColor(blockState, level, pos, quad.colorIndex());
				color |= 0xFF000000;
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
			blockInfo.prepareForBlock(
					generalOverlay ? TOP_SLAB : blockState,
					pos,
					model.useAmbientOcclusion(),
					context.getModelData(),
					renderType);
			if (generalOverlay) {
				((BlockRenderInfoAccess) blockInfo).setDefaultLayer(RenderType.cutout());
				blockInfo.blockPos = pos.below();
				for (Direction direction : Direction.Plane.HORIZONTAL) {
					context.isFaceCulled(direction);
				}
				blockInfo.blockPos = pos;
			}
		}
		((FabricBakedModel) model).emitBlockQuads(level, blockState, pos, randomSupplier, context);
		context.popTransform();
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
			if (!rawState.isAir() && !shouldRenderFace(camo, rawState, neighborPos, direction)) {
				return true;
			}
			BlockState snowState = snowVariant.srm$getSnowState(neighborState, level, neighborPos);
			return !snowState.isAir() && !shouldRenderFace(camo, snowState, neighborPos, direction);
		}
		return !Block.shouldRenderFace(camo, level, pos, direction, neighborPos);
	}

	/**
	 * Mirrors {@link Block#shouldRenderFace(BlockState, BlockGetter, BlockPos, Direction, BlockPos)} but compares
	 * against an explicitly given neighbor state instead of the state at the neighbor position.
	 */
	private boolean shouldRenderFace(BlockState state, BlockState neighborState, BlockPos neighborPos, Direction direction) {
		if (state.skipRendering(neighborState, direction)) {
			return false;
		}
		if (!neighborState.canOcclude()) {
			return true;
		}
		VoxelShape shape = state.getFaceOcclusionShape(level, pos, direction);
		if (shape.isEmpty()) {
			return true;
		}
		VoxelShape neighborShape = neighborState.getFaceOcclusionShape(level, neighborPos, direction.getOpposite());
		return Shapes.joinIsNotEmpty(shape, neighborShape, BooleanOp.ONLY_FIRST);
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
