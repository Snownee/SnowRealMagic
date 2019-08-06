package snownee.snow.client;

import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.snow.MainModule;
import snownee.snow.entity.FallingSnowEntity;

public class FallingSnowRenderer extends EntityRenderer<FallingSnowEntity>
{
    public FallingSnowRenderer(EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void doRender(FallingSnowEntity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (entity.getLayers() > 0 && entity.getLayers() <= 8)
        {
            BlockState state = MainModule.BLOCK.getDefaultState().with(SnowBlock.LAYERS, entity.getLayers());
            if (state.getRenderType() == BlockRenderType.MODEL)
            {
                World world = entity.world;

                if (state != world.getBlockState(new BlockPos(entity)))
                {
                    this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.disableLighting();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();

                    if (this.renderOutlines)
                    {
                        GlStateManager.enableColorMaterial();
                        GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
                    }

                    bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
                    BlockPos blockpos = new BlockPos(entity.posX, entity.getBoundingBox().maxY, entity.posZ);
                    GlStateManager.translatef((float) (x - blockpos.getX() - 0.5D), (float) (y - blockpos.getY()), (float) (z - blockpos.getZ() - 0.5D));
                    BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                    blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(state), state, blockpos, bufferbuilder, false, new Random(), state.getPositionRandom(entity.getOrigin()));
                    tessellator.draw();

                    if (this.renderOutlines)
                    {
                        GlStateManager.tearDownSolidRenderingTextureCombine();
                        GlStateManager.disableColorMaterial();
                    }

                    GlStateManager.enableLighting();
                    GlStateManager.popMatrix();
                    super.doRender(entity, x, y, z, entityYaw, partialTicks);
                }
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(FallingSnowEntity entity)
    {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}
