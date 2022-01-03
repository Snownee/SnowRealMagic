package snownee.snow.mixin.terraforged;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.terraforged.mod.chunk.column.post.LayerDecorator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import snownee.snow.compat.terraforged.TerraForgedModule;

@Mixin(LayerDecorator.class)
public abstract class MixinLayerDecorator {

	@Redirect(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/block/Block;)Z"
			), method = "fixBaseBlock"
	)
	private boolean srm_isIn(BlockState state, Block block) {
		return TerraForgedModule.isIn(state, block);
	}

}
