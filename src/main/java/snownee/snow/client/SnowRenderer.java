package snownee.snow.client;

import java.util.Objects;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import snownee.snow.block.SnowTile;

import javax.annotation.Nonnull;

@Deprecated
@OnlyIn(Dist.CLIENT)
public class SnowRenderer extends TileEntityRenderer<SnowTile> {
	public SnowRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	protected static final Random RAND = new Random();
	protected static BlockRendererDispatcher blockRenderer;

	@Override
	public void render(SnowTile te, float partialTicks, @Nonnull MatrixStack matrixstack, @Nonnull IRenderTypeBuffer buffer, int light, int otherlight) {
		if (!te.hasWorld() || te.getBlockState().get(SnowBlock.LAYERS) == 8) {
			return;
		}
		BlockState state = te.getState();
		if (state.isAir()) {
			return;
		}
		BlockPos pos = te.getPos();
		BlockModelRenderer.enableCache();
		matrixstack.push();
		if (blockRenderer == null)
			blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		IBakedModel model = blockRenderer.getModelForState(state);
		IBlockDisplayReader world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
		if (world == null) {
			world = te.getWorld();
		}

		RenderType rendertype = RenderType.getCutout();
		ForgeHooksClient.setRenderLayer(rendertype);
		IVertexBuilder ivertexbuilder = buffer.getBuffer(rendertype);
		blockRenderer.getBlockModelRenderer().renderModel(Objects.requireNonNull(world), model, state, pos, matrixstack, ivertexbuilder, false, RAND, state.getPositionRandom(pos), otherlight, EmptyModelData.INSTANCE);

		ForgeHooksClient.setRenderLayer(null);

		matrixstack.pop();
		BlockModelRenderer.disableCache();
	}

}
