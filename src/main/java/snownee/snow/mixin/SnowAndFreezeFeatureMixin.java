package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import snownee.snow.Hooks;

@Mixin(SnowAndFreezeFeature.class)
public class SnowAndFreezeFeatureMixin {

	@WrapOperation(
			method = "place",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean srm_place(
			Biome biome,
			LevelReader level,
			BlockPos pos,
			Operation<Boolean> original,
			@Local(argsOnly = true) WorldGenLevel worldGenLevel,
			@Local(name = "belowPos") BlockPos.MutableBlockPos belowPos) {
		boolean result = original.call(biome, level, pos);
		if (!result) {
			Hooks.placeFeatureExtra(biome, worldGenLevel, pos, belowPos);
		}
		return result;
	}

}
