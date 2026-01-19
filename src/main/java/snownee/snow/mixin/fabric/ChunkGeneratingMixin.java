package snownee.snow.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import snownee.snow.Hooks;

@Mixin(ChunkStatusTasks.class)
abstract class ChunkGeneratingMixin {
	@ModifyReturnValue(method = "lambda$full$0", at = @At("TAIL"))
	private static ChunkAccess onChunkLoad(final ChunkAccess original, @Local(name = "protoChunk") ProtoChunk protoChunk) {
		if (!(protoChunk instanceof ImposterProtoChunk)) {
			Hooks.restoreOriginalBlocks((LevelChunk) original);
		}
		return original;
	}
}
