package snownee.snow.client;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import snownee.snow.SnowRealMagic;

public class FabricRendererRenderAPI implements RenderAPI {

	private final BlockAndTintGetter level;
	private final RenderContext context;
	private final @Nullable RenderType renderType;
	private final Supplier<RandomSource> randomSupplier;
	private final BlockState selfState;
	private final BakedModel unwrapped;

	public FabricRendererRenderAPI(
			BlockAndTintGetter level,
			RenderContext context,
			@Nullable RenderType renderType,
			Supplier<RandomSource> randomSupplier,
			BlockState selfState,
			BakedModel unwrapped) {
		this.level = level;
		this.context = context;
		this.renderType = renderType;
		this.randomSupplier = randomSupplier;
		this.selfState = selfState;
		this.unwrapped = unwrapped;
	}

	@Override
	public boolean render(BlockState blockState, BlockPos pos, boolean cullSides, BakedModel model, double yOffset, ModelPart part) {
		for (Direction direction : Direction.values()) {
			boolean faceCulled = context.isFaceCulled(direction);
			SnowRealMagic.LOGGER.info("{} {}", direction, faceCulled);
		}

		Vec3 offset = yOffset == 0 ? blockState.getOffset(level, pos) : blockState.getOffset(level, pos).add(0, yOffset, 0);
		BlockColors blockColors = Minecraft.getInstance().getBlockColors();
		context.pushTransform(quad -> {
			if (blockState.is(Blocks.SNOW) && quad.cullFace() == Direction.DOWN && yOffset != 0) { // is slab
				return false;
			}
			int color = -1;
			if (quad.colorIndex() != -1) {
				color = blockColors.getColor(blockState, level, pos, quad.colorIndex());
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
		if (blockState == selfState && model != SnowClient.cachedOverlayModel) {
			model = unwrapped;
		}
		((FabricBakedModel) model).emitBlockQuads(level, blockState, pos, randomSupplier, context);
		context.popTransform();
		return true;
	}

}
