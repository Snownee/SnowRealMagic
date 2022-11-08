package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.MinecraftForgeClient;
import snownee.snow.BufferBuilderDuck;
import snownee.snow.SnowTile;

import snownee.snow.compat.NoTreePunchingCompat;
import snownee.snow.compat.PyrotechCompat;
import snownee.snow.compat.PyrotechUBCCompat;

@Mixin(BlockRendererDispatcher.class)
public abstract class MixinBlockRendererDispatcher {

	@Shadow
	@Final
	private BlockModelRenderer blockModelRenderer;

	@Inject(method = "renderBlock", at = @At("RETURN"))
	private void srm_renderBlock(IBlockState blockStateIn, BlockPos pos, IBlockAccess world, BufferBuilder bufferBuilderIn, CallbackInfoReturnable<Boolean> info) {
		if (blockStateIn.getBlock() == Blocks.SNOW_LAYER && blockStateIn.getValue(BlockSnow.LAYERS) < 8) {
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof SnowTile) {
				IBlockState state = ((SnowTile) tileEntity).getState();
				BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
				if (!state.getBlock().canRenderInLayer(state, layer))
					return;
				try {
					if (state.getRenderType() == EnumBlockRenderType.MODEL) {
						if (world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
							try {
								state = state.getActualState(world, pos);
							} catch (Exception e) {
							}
						}
						//matrixStackIn.push();
						IBakedModel model = getModelForState(state);
						state = state.getBlock().getExtendedState(state, world, pos);
						boolean translate = (NoTreePunchingCompat.isRock(state.getBlock()) || PyrotechCompat.isRock(state.getBlock()) || PyrotechUBCCompat.isRock(state.getBlock()));
						if (translate) {
							((BufferBuilderDuck) bufferBuilderIn).translateY(0.125);
						}
						blockModelRenderer.renderModel(world, model, state, pos, bufferBuilderIn, true);
						if (translate) {
							((BufferBuilderDuck) bufferBuilderIn).translateY(-0.125);
						}
						//matrixStackIn.pop();
					}
				} catch (Throwable throwable) {
					CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
					CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
					CrashReportCategory.addBlockInfo(crashreportcategory, pos, blockStateIn);
					throw new ReportedException(crashreport);
				}
			}
		}
	}

	@Shadow
	public abstract IBakedModel getModelForState(IBlockState state);

}
