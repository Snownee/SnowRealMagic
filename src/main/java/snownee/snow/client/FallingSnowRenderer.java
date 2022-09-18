package snownee.snow.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.entity.FallingSnowEntity;

@Environment(EnvType.CLIENT)
public class FallingSnowRenderer extends EntityRenderer<FallingSnowEntity> {
	public FallingSnowRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		shadowRadius = 0.5F;
	}

	@Override
	public void render(FallingSnowEntity entity, float p_225623_2_, float p_225623_3_, PoseStack matrixstack, MultiBufferSource buffer, int p_225623_6_) {
		if (entity.getLayers() <= 0 && entity.getLayers() > 8) {
			return;
		}
		BlockState blockstate = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, entity.getLayers());
		if (blockstate.getRenderShape() != RenderShape.MODEL) {
			return;
		}
		Level world = entity.getLevel();

		matrixstack.pushPose();
		BlockPos blockpos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
		matrixstack.translate(-0.5D, 0.0D, -0.5D);
		BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
		RenderType type = ItemBlockRenderTypes.getMovingBlockRenderType(blockstate);
		blockrendererdispatcher.getModelRenderer().tesselateBlock(world, blockrendererdispatcher.getBlockModel(blockstate), blockstate, blockpos, matrixstack, buffer.getBuffer(type), false, RandomSource.create(42), blockstate.getSeed(entity.getOrigin()), OverlayTexture.NO_OVERLAY);
		matrixstack.popPose();
		super.render(entity, p_225623_2_, p_225623_3_, matrixstack, buffer, p_225623_6_);
	}

	@Override
	public ResourceLocation getTextureLocation(FallingSnowEntity entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}

}
