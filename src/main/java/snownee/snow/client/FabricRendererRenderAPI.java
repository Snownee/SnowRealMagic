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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import snownee.snow.mixin.client.AbstractBlockRenderContextAccess;

public class FabricRendererRenderAPI implements RenderAPI {

	private static final BlockState TOP_SLAB = Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
	private final BlockAndTintGetter level;
	private final RenderContext context;
	private final @Nullable RenderType renderType;
	private final Supplier<RandomSource> randomSupplier;
	private final BlockState selfState;
	private final BlockPos pos;
	private final BakedModel unwrapped;

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
		Vec3 offset = yOffset == 0 ? blockState.getOffset(level, pos) : blockState.getOffset(level, pos).add(0, yOffset, 0);
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
			int color = -1;
			if (quad.colorIndex() != -1) {
				color = Minecraft.getInstance().getBlockColors().getColor(blockState, level, pos, quad.colorIndex());
				color |= 0xFF000000;
			}
			if (offset != Vec3.ZERO || color != -1) {
				for (int i = 0; i < 4; ++i) {
					quad.pos(i, quad.x(i) + (float) offset.x, quad.y(i) + (float) offset.y, quad.z(i) + (float) offset.z);
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
					model.useAmbientOcclusion());
			if (generalOverlay) {
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

	@Override
	public BlockAndTintGetter level() {
		return level;
	}

	@Override
	public BlockPos pos() {
		return pos;
	}

}
