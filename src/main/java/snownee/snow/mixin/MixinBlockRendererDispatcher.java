package snownee.snow.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowTile;

@Mixin(BlockRendererDispatcher.class)
public abstract class MixinBlockRendererDispatcher {

	@Shadow
	@Final
	private BlockModelRenderer blockModelRenderer;

	@Inject(
			method = "renderModel(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockDisplayReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;ZLjava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z", at = @At(
				"RETURN"
			), remap = false
	)
	private void srm_renderModel(BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, boolean checkSides, Random rand, IModelData modelData, CallbackInfoReturnable<Boolean> info) {
		if (blockStateIn.getBlock() == CoreModule.TILE_BLOCK && blockStateIn.get(BlockStateProperties.LAYERS_1_8) < 8) {
			TileEntity tileEntity = lightReaderIn.getTileEntity(posIn);
			if (tileEntity instanceof SnowTile) {
				BlockState state = ((SnowTile) tileEntity).getState();
				try {
					if (state.getRenderType() == BlockRenderType.MODEL) {
						String namespace = state.getBlock().getRegistryName().getNamespace();
						if (namespace.equals("projectvibrantjourneys") || namespace.equals("foragecraft")) {
							if (blockStateIn.get(BlockStateProperties.LAYERS_1_8) > 3)
								return;
							matrixStackIn.push();
							matrixStackIn.translate(0.001, 0.101, 0.001);
							matrixStackIn.scale(0.998f, 1, 0.998f);
						} else {
							matrixStackIn.push();
						}
						blockModelRenderer.renderModel(lightReaderIn, getModelForState(state), state, posIn, matrixStackIn, vertexBuilderIn, false, rand, state.getPositionRandom(posIn), OverlayTexture.NO_OVERLAY, modelData);
						matrixStackIn.pop();
					}
				} catch (Throwable throwable) {
					CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
					CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
					CrashReportCategory.addBlockInfo(crashreportcategory, posIn, blockStateIn);
					throw new ReportedException(crashreport);
				}
			}
		}
	}

	@Shadow
	public abstract IBakedModel getModelForState(BlockState state);
}
