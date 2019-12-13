package snownee.snow.client;

import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import snownee.snow.block.SnowTile;

@OnlyIn(Dist.CLIENT)
public class SnowRenderer extends TileEntityRenderer<SnowTile> {
    private static final Random RAND = new Random();

    @Override
    public void render(SnowTile te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!te.hasWorld() || te.getBlockState().get(SnowBlock.LAYERS) == 8) {
            return;
        }
        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableDepthTest();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        GlStateManager.enableRescaleNormal();
        bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        buffer.begin(7, DefaultVertexFormats.BLOCK);
        renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        GlStateManager.disableBlend();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void renderTileEntityFast(SnowTile te, double x, double y, double z, float partialTicks, int destroyStage, BufferBuilder buffer) {
        if (!te.hasWorld() || te.getBlockState().get(SnowBlock.LAYERS) == 8) {
            return;
        }
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockPos pos = te.getPos();
        BlockState state = te.getState();
        buffer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
        dispatcher.renderBlock(state, te.getPos(), te.getWorld(), buffer, RAND, EmptyModelData.INSTANCE);
    }
}
