package snownee.snow.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.snow.SnowAccumulation;
import snownee.snow.SnowCommonConfig;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
	// The snow-covered blocks are converted back to vanilla snow during world generation, leaving
	// a pending "DUMMY" block entity behind. Vanilla then spams the log when promoting it.
	@Inject(
			method = "promotePendingBlockEntity",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
					remap = false,
					ordinal = 0),
			cancellable = true)
	private void srm_cancelPendingBlockEntityLogSpam(
			BlockPos pos,
			CompoundTag tag,
			CallbackInfoReturnable<@Nullable BlockEntity> cir,
			@Local(type = BlockState.class) BlockState state) {
		if (SnowCommonConfig.preventWorldgenLogSpam && state.is(Blocks.SNOW)) {
			cir.setReturnValue(null);
		}
	}

	@Inject(
			method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
			at = @At("HEAD"))
	private void srm_clearSnowAccumulation(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<BlockState> cir) {
		LevelChunk chunk = (LevelChunk) (Object) this;
		if (chunk.getLevel().isClientSide()) {
			return;
		}
		BlockState oldState = chunk.getBlockState(pos);
		if (oldState.getBlock() != newState.getBlock() && SnowAccumulation.isSupported(oldState)) {
			SnowAccumulation.clear(chunk, pos);
		}
	}
}
