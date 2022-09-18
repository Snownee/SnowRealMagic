package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.snow.WorldTickHandler;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

	@Redirect(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", remap = false
			), slice = @Slice(
			from = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/SkeletonHorse;setTrap(Z)V"
			), to = @At(
			value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"
	)
	), method = "tickChunk"
	)
	private int cancelVanillaSnow(RandomSource random, int bound, LevelChunk chunk, int tickSpeed) {
		WorldTickHandler.tick((ServerLevel) (Object) this, chunk, random);
		return -1;
	}

}
