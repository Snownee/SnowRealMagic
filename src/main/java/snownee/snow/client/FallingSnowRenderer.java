package snownee.snow.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import snownee.snow.CoreModule;
import snownee.snow.entity.FallingSnowEntity;

@OnlyIn(Dist.CLIENT)
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
		BlockState blockstate = CoreModule.BLOCK.defaultBlockState().setValue(SnowLayerBlock.LAYERS, entity.getLayers());
		if (blockstate.getRenderShape() != RenderShape.MODEL) {
			return;
		}
		Level world = entity.getLevel();

		matrixstack.pushPose();
		BlockPos blockpos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
		matrixstack.translate(-0.5D, 0.0D, -0.5D);
		BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = blockrendererdispatcher.getBlockModel(blockstate);
		RandomSource random = RandomSource.create(42);
		for (RenderType type : model.getRenderTypes(blockstate, random, ModelData.EMPTY)) {
			blockrendererdispatcher.getModelRenderer().tesselateBlock(world, model, blockstate, blockpos, matrixstack, buffer.getBuffer(type), false, random, blockstate.getSeed(entity.getOrigin()), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, type);
		}
		matrixstack.popPose();
		super.render(entity, p_225623_2_, p_225623_3_, matrixstack, buffer, p_225623_6_);
	}

	@Override
	public ResourceLocation getTextureLocation(FallingSnowEntity entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}

}
