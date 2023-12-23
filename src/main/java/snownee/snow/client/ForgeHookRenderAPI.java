package snownee.snow.client;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class ForgeHookRenderAPI implements RenderAPI {

	private final ModelData modelData;
	private final PoseStack matrixStack;
	private final VertexConsumer vertexBuilder;

	public ForgeHookRenderAPI(ModelData modelData, PoseStack matrixStack, VertexConsumer vertexBuilder) {
		this.modelData = modelData;
		this.matrixStack = matrixStack;
		this.vertexBuilder = vertexBuilder;
	}

	@Override
	public boolean translateYAndRender(BlockAndTintGetter world, BlockState state, BlockPos pos, @Nullable RenderType layer, Supplier<RandomSource> randomSupplier, boolean cullSides, BakedModel model, double yOffset) {
		RandomSource random = randomSupplier.get();
		if (layer != null && !model.getRenderTypes(state, random, modelData).contains(layer)) {
			return false;
		}
		matrixStack.pushPose();
		if (yOffset != 0) {
			matrixStack.translate(-0.001, yOffset, -0.001);
			matrixStack.scale(1.002f, 1, 1.002f);
			cullSides = false;
		}
		ModelData data = model.getModelData(world, pos, state, modelData);
		Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(world, model, state, pos, matrixStack, vertexBuilder, cullSides, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY, data, layer);
		matrixStack.popPose();
		return true;
	}

}
