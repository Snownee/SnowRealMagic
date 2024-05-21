package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.snow.WorldTickHandler;
import snownee.snow.util.CommonProxy;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

	/**
	 * @reason We don't inject into "snowandice" body because sometimes we want to customize the random chance
	 */
	@SuppressWarnings("UnreachableCode")
	@Inject(
			method = "tickChunk",
			at = @At(
					value = "INVOKE",
					ordinal = 1,
					target = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
	)
	private void srm_tickSnow(LevelChunk chunk, int tickBlocks, CallbackInfo ci) {
		ServerLevel level = (ServerLevel) (Object) this;
		CommonProxy.weatherTick(level, () -> WorldTickHandler.tick(level, chunk));
	}
}
