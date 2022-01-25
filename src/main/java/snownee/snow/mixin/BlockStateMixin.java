package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.material.Material;
import snownee.snow.CoreModule;

@Mixin(BlockStateBase.class)
public class BlockStateMixin {

	@Inject(at = @At("HEAD"), method = "getMaterial", cancellable = true)
	private void srm_getMaterial(CallbackInfoReturnable<Material> ci) {
		BlockStateBase state = (BlockStateBase) (Object) this;
		if (state.getBlock() == Blocks.SNOW || state.getBlock() == CoreModule.TILE_BLOCK) {
			if (state.getValue(SnowLayerBlock.LAYERS) == 8) {
				ci.setReturnValue(Material.SNOW);
			}
		}
	}

}
