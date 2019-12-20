package snownee.snow.client;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.snow.MainModule;
import snownee.snow.entity.FallingSnowEntity;

public class FallingSnowRenderer extends EntityRenderer<FallingSnowEntity> {
    public FallingSnowRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void func_225623_a_/*doRender*/(FallingSnowEntity entity, float p_225623_2_, float p_225623_3_, MatrixStack matrixstack, IRenderTypeBuffer buffer, int p_225623_6_) {
        if (entity.getLayers() <= 0 && entity.getLayers() > 8) {
            return;
        }
        BlockState blockstate = MainModule.BLOCK.getDefaultState().with(SnowBlock.LAYERS, entity.getLayers());
        if (blockstate.getRenderType() != BlockRenderType.MODEL) {
            return;
        }
        World world = entity.getWorldObj();

        matrixstack.func_227860_a_();
        BlockPos blockpos = new BlockPos(entity.func_226277_ct_(), entity.getBoundingBox().maxY, entity.func_226281_cx_());
        matrixstack.func_227861_a_(-0.5D, 0.0D, -0.5D);
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.func_228661_n_()) {
            if (RenderTypeLookup.canRenderInLayer(blockstate, type)) {
                net.minecraftforge.client.ForgeHooksClient.setRenderLayer(type);
                blockrendererdispatcher.getBlockModelRenderer().func_228802_a_(world, blockrendererdispatcher.getModelForState(blockstate), blockstate, blockpos, matrixstack, buffer.getBuffer(type), false, new Random(), blockstate.getPositionRandom(entity.getOrigin()), OverlayTexture.field_229196_a_);
            }
        }
        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
        matrixstack.func_227865_b_();
        super.func_225623_a_(entity, p_225623_2_, p_225623_3_, matrixstack, buffer, p_225623_6_);
    }

    @Override
    public ResourceLocation getEntityTexture(FallingSnowEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}
