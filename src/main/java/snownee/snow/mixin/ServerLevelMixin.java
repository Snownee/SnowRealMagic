package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.snow.WorldTickHandler;
import snownee.snow.util.CommonProxy;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

	/**
	 * @reason We dont inject into "snowandice" body because sometimes we want to customize the random chance
	 */
	@Inject(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"), slice = @Slice(
			from = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/SkeletonHorse;setTrap(Z)V"
			), to = @At(
			value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"
	)
	), method = "tickChunk", locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	private void srm_cancelVanillaWeather(LevelChunk chunk, int tickBlocks, CallbackInfo ci) {
		ServerLevel level = (ServerLevel) (Object) this;
		CommonProxy.weatherTick(level, () -> WorldTickHandler.tick(level, chunk));
	}

}
