package snownee.snow;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TESRSnowLayer extends FastTESR<TileSnowLayer>
{
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
