package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import snownee.snow.SnowAccumulation;

@Mixin(Snowball.class)
public class SnowballMixin {

	@Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"))
	private void srm_addSnowToLeaves(HitResult hitResult, CallbackInfo ci) {
		if (!(hitResult instanceof BlockHitResult blockHit)) {
			return;
		}
		Snowball snowball = (Snowball) (Object) this;
		Level level = snowball.level();
		if (level.isClientSide()) {
			return;
		}
		BlockPos center = blockHit.getBlockPos();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!level.isLoaded(pos)) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (!SnowAccumulation.isSupported(state)) {
						continue;
					}
					LevelChunk chunk = level.getChunkAt(pos);
					SnowAccumulation.increase(chunk, pos);
				}
			}
		}
	}
}
