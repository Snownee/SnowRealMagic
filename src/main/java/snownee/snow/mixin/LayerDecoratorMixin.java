package snownee.snow.mixin;
/*
package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.terraforged.mod.chunk.column.post.LayerDecorator;

import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.compat.terraforged.TerraForgedModule;

@Mixin(LayerDecorator.class)
public abstract class LayerDecoratorMixin {

	@Redirect(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/block/Block;)Z"
			), method = "fixBaseBlock"
	)
	private boolean srm_isIn(BlockState state, Block block) {
		return TerraForgedModule.isIn(state, block);
	}

}
*/
