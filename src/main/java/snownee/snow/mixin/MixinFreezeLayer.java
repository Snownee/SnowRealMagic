package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.terraforged.mod.feature.feature.FreezeLayer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import snownee.snow.compat.terraforged.TerraForgedModule;

@Mixin(FreezeLayer.class)
public class MixinFreezeLayer {

	@Inject(at = @At("TAIL"), method = "freezeGround", remap = false)
	private void srm_freezeGround(IWorld world, IChunk chunk, Biome biome, BlockPos.Mutable snowPos, BlockPos.Mutable underPos, CallbackInfo info) {
		TerraForgedModule.freezeGround(world, chunk, biome, snowPos, underPos);
	}

}
