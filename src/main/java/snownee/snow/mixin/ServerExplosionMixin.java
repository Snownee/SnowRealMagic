package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.ServerExplosion;
import snownee.snow.SnowAccumulation;

@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {

	@Inject(method = "explode", at = @At("HEAD"))
	private void srm_clearLeafSnowByWindCharge(CallbackInfoReturnable<Integer> cir) {
		ServerExplosion explosion = (ServerExplosion) (Object) this;
		if (explosion.canTriggerBlocks()) {
			// The wind charge pushes entities within twice its explosion radius, so blow snow off that same area.
			SnowAccumulation.clearAround(explosion.level(), explosion.center(), explosion.radius() * 2.0D);
		}
	}
}
