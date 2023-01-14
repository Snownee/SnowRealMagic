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
import snownee.snow.client.model.SnowCoveredModel;

public class FabricRendererRenderAPI implements RenderAPI {

	private final RenderContext context;

	public FabricRendererRenderAPI(RenderContext context) {
		this.context = context;
	}

	@Override
	public boolean translateYAndRender(BlockAndTintGetter world, BlockState state, BlockPos pos, @Nullable RenderType layer, Supplier<RandomSource> randomSupplier, boolean cullSides, BakedModel model, double yOffset) {
		Vec3 offset = yOffset == 0 ? state.getOffset(world, pos) : state.getOffset(world, pos).add(0, yOffset, 0);
		BlockColors blockColors = Minecraft.getInstance().getBlockColors();
		context.pushTransform(quad -> {
			if (state.is(Blocks.SNOW) && quad.cullFace() == Direction.DOWN && yOffset != 0) { // is slab
				return false;
			}
			int color = -1;
			if (quad.colorIndex() != -1) {
				color = blockColors.getColor(state, world, pos, quad.colorIndex());
				color |= 0xFF000000;
			}
			if (offset != Vec3.ZERO || color != -1) {
				for (int i = 0; i < 4; ++i) {
					quad.pos(i, quad.x(i) + (float) offset.x, quad.y(i) + (float) offset.y, quad.z(i) + (float) offset.z);
					quad.spriteColor(i, 0, color);
				}
			}
			return true;
		});
		if (model instanceof SnowCoveredModel snowCovered) {
			model = snowCovered.getWrappedModel();
		}
		((FabricBakedModel) model).emitBlockQuads(world, state, pos, randomSupplier, context);
		context.popTransform();
		return true;
	}

}
