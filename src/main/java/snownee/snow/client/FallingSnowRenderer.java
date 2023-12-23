package snownee.snow.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.entity.FallingSnowEntity;
import snownee.snow.util.ClientProxy;

public class FallingSnowRenderer extends EntityRenderer<FallingSnowEntity> {
	public FallingSnowRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		shadowRadius = 0.5F;
	}

	@Override
	public void render(FallingSnowEntity entity, float p_225623_2_, float p_225623_3_, PoseStack poseStack, MultiBufferSource bufferSource, int p_225623_6_) {
		BlockState blockstate = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, entity.getLayers());
		if (blockstate.getRenderShape() != RenderShape.MODEL) {
			return;
		}
		poseStack.pushPose();
		ClientProxy.renderFallingBlock(entity, blockstate, entity.getStartPos(), poseStack, bufferSource);
		poseStack.popPose();
		super.render(entity, p_225623_2_, p_225623_3_, poseStack, bufferSource, p_225623_6_);
	}

	@Override
	public ResourceLocation getTextureLocation(FallingSnowEntity entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}

}
