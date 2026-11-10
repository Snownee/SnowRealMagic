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
import snownee.snow.SnowCommonConfig;

@Mixin(SnowAndFreezeFeature.class)
public class SnowAndFreezeFeatureMixin {

	@WrapOperation(
			method = "place",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/biome/Biome;shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z"))
	private boolean srm_placeIce(
			Biome biome,
			LevelReader level,
			BlockPos pos,
			boolean checkNeighbors,
			Operation<Boolean> original) {
		boolean result = original.call(biome, level, pos, checkNeighbors);
		if (!result && SnowCommonConfig.worldGenSnowAndFreezeEverywhere) {
			result = Hooks.shouldFreezeIgnoringTemperature(level, pos);
		}
		return result;
	}

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
		if (!result && SnowCommonConfig.worldGenSnowAndFreezeEverywhere) {
			result = Hooks.shouldSnowIgnoringTemperature(level, pos);
		}
		if (!result) {
			Hooks.placeFeatureExtra(biome, worldGenLevel, pos, belowPos);
		}
		return result;
	}

}
