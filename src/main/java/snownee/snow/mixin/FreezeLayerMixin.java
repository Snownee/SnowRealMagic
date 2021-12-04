package snownee.snow.mixin;
/*
package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.terraforged.mod.feature.feature.FreezeLayer;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import snownee.snow.compat.terraforged.TerraForgedModule;

@Mixin(FreezeLayer.class)
public class FreezeLayerMixin {

	@Inject(at = @At("TAIL"), method = "freezeGround", remap = false)
	private void srm_freezeGround(LevelAccessor world, ChunkAccess chunk, Biome biome, MutableBlockPos snowPos, MutableBlockPos underPos, CallbackInfo info) {
		TerraForgedModule.freezeGround(world, chunk, biome, snowPos, underPos);
	}

}
*/