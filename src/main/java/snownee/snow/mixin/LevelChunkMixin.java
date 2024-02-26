package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.snow.SnowCommonConfig;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
	//TODO: use MixinExtras
	@Inject(
			method = "promotePendingBlockEntity", at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
			remap = false,
			ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void snow$cancelPendingBlockEntityLogSpam(
			BlockPos pos, CompoundTag tag, CallbackInfoReturnable<BlockEntity> cir, BlockState blockstate, BlockEntity blockentity) {
		if (SnowCommonConfig.preventWorldgenLogSpam && blockstate.is(Blocks.SNOW)) {
			cir.setReturnValue(null);
		}
	}
}
