package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.Blocks;
import snownee.snow.block.ShapeCaches;

@Mixin(Blocks.class)
public class BlocksMixin {

	@Inject(method = "rebuildCache", at = @At("RETURN"))
	private static void rebuildCache(CallbackInfo ci) {
		ShapeCaches.invalidateAll();
	}

}
