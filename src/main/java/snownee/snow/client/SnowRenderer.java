package snownee.snow.client;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import snownee.snow.block.SnowTile;

@OnlyIn(Dist.CLIENT)
public class SnowRenderer extends TileEntityRenderer<SnowTile> {
    public SnowRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    protected static final Random RAND = new Random();
    protected static BlockRendererDispatcher blockRenderer;

    @Override
    public void func_225616_a_/*render*/(SnowTile te, float partialTicks, MatrixStack matrixstack, IRenderTypeBuffer buffer, int light, int otherlight) {
        if (!te.hasWorld() || te.getBlockState().get(SnowBlock.LAYERS) == 8) {
            return;
        }
        BlockModelRenderer.enableCache();
        matrixstack.func_227860_a_();
        if (blockRenderer == null)
            blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockPos pos = te.getPos();
        BlockState state = te.getState();
        IBakedModel model = blockRenderer.getModelForState(state);
        ILightReader world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
        blockRenderer.getBlockModelRenderer().renderModel(world, model, state, pos, matrixstack, buffer.getBuffer(RenderTypeLookup.func_228394_b_(state)), false, RAND, state.getPositionRandom(pos), light, EmptyModelData.INSTANCE);
        matrixstack.func_227865_b_();
        BlockModelRenderer.disableCache();
    }
}
