package snownee.snow;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TESRSnowLayer extends TileEntitySpecialRenderer<TileSnowLayer>
{
    @Override
    public void render(TileSnowLayer te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        if (!te.hasWorld())
        {
            return;
        }
        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1, 1, 1, 1);
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.BLOCK);
        renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partialTicks, buffer);
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void renderTileEntityFast(TileSnowLayer te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer)
    {
        if (!te.hasWorld())
        {
            return;
        }
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        BlockPos pos = te.getPos();
        IBlockState state = te.getState();
        buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        dispatcher.renderBlock(state, te.getPos(), te.getWorld(), buffer);
    }
}
