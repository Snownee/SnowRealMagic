package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@Mixin(value = IceBlock.class, priority = 500)
public class IceBlockMixin {

	@Inject(method = "melt", at = @At("HEAD"), cancellable = true)
	private void srm_melt(BlockState blockState, Level level, BlockPos blockPos, CallbackInfo ci) {
		BlockPos above = blockPos.above();
		BlockState stateAbove = level.getBlockState(above);
		if (stateAbove.getBlock() instanceof SnowLayerBlock) {
			if (stateAbove.getValue(BlockStateProperties.LAYERS) < 5) {
				level.removeBlock(above, false);
			} else {
				ci.cancel();
			}
		}
	}

}